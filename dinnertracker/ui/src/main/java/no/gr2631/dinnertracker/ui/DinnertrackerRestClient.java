package no.gr2631.dinnertracker.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.User;

/**
 * Client for the Dinnertracker REST API.
 *
 * <p>All data the JavaFX application reads or saves goes through this class. It sends HTTP requests
 * to the REST server and converts the JSON bodies to and from the core classes, so the controllers
 * never touch the JSON files directly.
 *
 * <p>The server address is read from the system property {@value #BASE_URL_PROPERTY} and defaults
 * to {@value #DEFAULT_BASE_URL}, which matches {@code server.port} in the REST module.
 */
public class DinnertrackerRestClient {

  /** System property that can be used to point the client at another server. */
  public static final String BASE_URL_PROPERTY = "dinnertracker.api.url";

  /** Server address used when {@link #BASE_URL_PROPERTY} is not set. */
  public static final String DEFAULT_BASE_URL = "http://localhost:7070";

  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private final String baseUrl;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  /** Creates a client for the server configured by {@link #BASE_URL_PROPERTY}. */
  public DinnertrackerRestClient() {
    this(
        URI.create(System.getProperty(BASE_URL_PROPERTY, DEFAULT_BASE_URL)),
        HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
  }

  /**
   * Creates a client for the given server, using the given HTTP client. Mainly used by tests.
   *
   * @param baseUri the server address, for example {@code http://localhost:7070}
   * @param httpClient the HTTP client used to send requests
   */
  DinnertrackerRestClient(URI baseUri, HttpClient httpClient) {
    String url = baseUri.toString();
    this.baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    this.httpClient = httpClient;
  }

  /**
   * Looks up a registered user ({@code GET /users}).
   *
   * @param userName the username to look up
   * @return the registered user, or {@code null} if no user has that name
   * @throws IOException if the server cannot be reached or answers with an error
   */
  public User getUser(String userName) throws IOException {
    HttpResponse<String> response = send(get(uriWithUser("/users", userName)));
    if (response.statusCode() == 404) {
      return null;
    }
    requireStatus(response, 200);
    return new User(response.body());
  }

  /**
   * Registers a new user ({@code POST /users}).
   *
   * @param user the user to register
   * @throws IOException if the server cannot be reached or does not create the user
   */
  public void createUser(User user) throws IOException {
    ObjectNode body = objectMapper.createObjectNode();
    body.put("userName", user.getUserName());
    HttpResponse<String> response = send(post(URI.create(baseUrl + "/users"), body));
    requireStatus(response, 201);
  }

  /**
   * Loads a user's fridge ({@code GET /fridges}).
   *
   * @param user the owner of the fridge; the returned fridge is linked to this exact object
   * @return the user's fridge, which is empty if nothing has been saved yet
   * @throws IOException if the server cannot be reached, answers with an error, or sends data that
   *     cannot be read
   */
  public Fridge getFridge(User user) throws IOException {
    HttpResponse<String> response = send(get(uriWithUser("/fridges", user.getUserName())));
    requireStatus(response, 200);
    try {
      return fridgeFromJson(objectMapper.readTree(response.body()), user);
    } catch (IllegalArgumentException e) {
      throw new IOException("Ugyldig kjøleskap fra serveren: " + e.getMessage(), e);
    }
  }

  /**
   * Replaces the stored contents of a fridge ({@code PUT /fridges}).
   *
   * @param fridge the fridge to save; it is stored under its owner's username
   * @throws IOException if the server cannot be reached or rejects the fridge
   */
  public void updateFridge(Fridge fridge) throws IOException {
    URI uri = uriWithUser("/fridges", fridge.getUserName().getUserName());
    HttpResponse<String> response = send(put(uri, fridgeToJson(fridge)));
    requireStatus(response, 200);
  }

  /**
   * Loads every recipe, from all users ({@code GET /recipes/all}). Recipes are shared, so everyone
   * sees the same list.
   *
   * <p>Each recipe keeps the user who created it. The logged-in user's own recipes are linked to
   * the given {@code user} object; recipes from other users get their own {@link User}.
   *
   * @param user the logged-in user, who owns the returned recipe book
   * @return a recipe book with all shared recipes
   * @throws IOException if the server cannot be reached, answers with an error, or sends data that
   *     cannot be read
   */
  public RecipeBook getRecipeBook(User user) throws IOException {
    HttpResponse<String> response = send(get(URI.create(baseUrl + "/recipes/all")));
    requireStatus(response, 200);
    RecipeBook recipeBook = new RecipeBook(user);
    try {
      for (JsonNode recipeNode : objectMapper.readTree(response.body())) {
        recipeBook.addRecipe(recipeFromJson(recipeNode, user));
      }
    } catch (IllegalArgumentException e) {
      throw new IOException("Ugyldig oppskrift fra serveren: " + e.getMessage(), e);
    }
    return recipeBook;
  }

  /**
   * Saves a new recipe ({@code POST /recipes}).
   *
   * @param recipe the recipe to save; it is stored under its owner's username
   * @throws IOException if the server cannot be reached or rejects the recipe
   */
  public void createRecipe(Recipe recipe) throws IOException {
    HttpResponse<String> response =
        send(post(URI.create(baseUrl + "/recipes"), recipeToJson(recipe)));
    requireStatus(response, 201);
  }

  // HTTP helpers

  /**
   * Builds a URI for the given path with the username as the {@code userName} query parameter.
   *
   * @param path the API path, for example {@code /fridges}
   * @param userName the username to put in the query; it is URL-encoded
   * @return the full URI, for example {@code http://localhost:7070/fridges?userName=ola}
   */

  private URI uriWithUser(String path, String userName) {
    // URLEncoder turns spaces into '+'; '%20' is understood the same way by every server.
    String encoded = URLEncoder.encode(userName, StandardCharsets.UTF_8).replace("+", "%20");
    return URI.create(baseUrl + path + "?userName=" + encoded);
  }

  /**
   * Builds a GET request that asks for JSON.
   *
   * @param uri the address to send the request to
   * @return the finished request
   */

  private HttpRequest get(URI uri) {
    return HttpRequest.newBuilder(uri)
        .timeout(TIMEOUT)
        .header("Accept", "application/json")
        .GET()
        .build();
  }

  /**
   * Builds a POST request with the given JSON body.
   *
   * @param uri the address to send the request to
   * @param body the JSON to send as the request body
   * @return the finished request
   * @throws IOException if the body cannot be converted to a JSON string
   */

  private HttpRequest post(URI uri, JsonNode body) throws IOException {
    return HttpRequest.newBuilder(uri)
        .timeout(TIMEOUT)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
        .build();
  }

  /**
   * Builds a PUT request with the given JSON body.
   *
   * @param uri the address to send the request to
   * @param body the JSON to send as the request body
   * @return the finished request
   * @throws IOException if the body cannot be converted to a JSON string
   */

  private HttpRequest put(URI uri, JsonNode body) throws IOException {
    return HttpRequest.newBuilder(uri)
        .timeout(TIMEOUT)
        .header("Content-Type", "application/json")
        .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
        .build();
  }

  /**
   * Sends a request and returns the response with the body as a string.
   *
   * <p>A failed connection or an interrupted request is turned into an {@link IOException} with a
   * readable message. If the thread is interrupted, its interrupt flag is set again.
   *
   * @param request the request to send
   * @return the response from the server
   * @throws IOException if the server cannot be reached or the request is interrupted
   */

  private HttpResponse<String> send(HttpRequest request) throws IOException {
    try {
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (ConnectException e) {
      throw new IOException(
          "Får ikke kontakt med serveren på " + baseUrl + ". Er REST-serveren startet?", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Forespørselen til serveren ble avbrutt.", e);
    }
  }

  /**
   * Checks that the server answered with the expected status code.
   *
   * @param response the response to check
   * @param expectedStatus the status code that counts as success, for example {@code 200}
   * @throws IOException if the status code is different; the message contains the status and the
   *     response body
   */

  private static void requireStatus(HttpResponse<String> response, int expectedStatus)
      throws IOException {
    if (response.statusCode() != expectedStatus) {
      throw new IOException(
          "Serveren svarte med status " + response.statusCode() + ": " + response.body());
    }
  }

  // JSON conversion. The format matches the REST controllers: units are sent as enum names.

  /**
   * Converts a fridge to the JSON format the REST server expects.
   *
   * @param fridge the fridge to convert
   * @return a JSON object with {@code userName} and a {@code foodItems} array
   */

  private ObjectNode fridgeToJson(Fridge fridge) {
    ObjectNode node = objectMapper.createObjectNode();
    node.put("userName", fridge.getUserName().getUserName());
    writeAmounts(node.putArray("foodItems"), fridge.getFoodItems());
    return node;
  }

  /**
   * Builds a fridge from the JSON the REST server sends.
   *
   * @param node the JSON object for the fridge
   * @param user the owner of the fridge; the returned fridge is linked to this object
   * @return a fridge that holds the food items from the JSON
   * @throws IllegalArgumentException if a food item is missing a name, a valid quantity or a unit
   */

  private static Fridge fridgeFromJson(JsonNode node, User user) {
    Fridge fridge = new Fridge(user);
    for (JsonNode itemNode : node.path("foodItems")) {
      fridge.addFoodItem(new FoodItem(requiredText(itemNode, "name")), readAmount(itemNode));
    }
    return fridge;
  }

  /**
   * Converts a recipe to the JSON format the REST server expects.
   *
   * @param recipe the recipe to convert
   * @return a JSON object with {@code userName}, {@code name}, {@code ingredients} and
   *     {@code steps}
   */

  private ObjectNode recipeToJson(Recipe recipe) {
    ObjectNode node = objectMapper.createObjectNode();
    node.put("userName", recipe.getUserName().getUserName());
    node.put("name", recipe.getName());
    writeAmounts(node.putArray("ingredients"), recipe.getIngredients());
    ArrayNode steps = node.putArray("steps");
    recipe.getSteps().forEach(steps::add);
    return node;
  }

  /**
   * Builds a recipe from the JSON the REST server sends.
   *
   * <p>If the recipe belongs to the logged-in user, it is linked to {@code currentUser}. Otherwise a
   * new {@link User} is created for the owner.
   *
   * @param node the JSON object for the recipe
   * @param currentUser the logged-in user
   * @return the recipe with its ingredients and steps
   * @throws IllegalArgumentException if a required field is missing or has the wrong type
   */

  private static Recipe recipeFromJson(JsonNode node, User currentUser) {
    String ownerName = requiredText(node, "userName");
    User owner = ownerName.equals(currentUser.getUserName()) ? currentUser : new User(ownerName);
    Recipe recipe = new Recipe(requiredText(node, "name"), owner);
    for (JsonNode ingredientNode : node.path("ingredients")) {
      recipe.addIngredient(
          new FoodItem(requiredText(ingredientNode, "name")), readAmount(ingredientNode));
    }
    for (JsonNode stepNode : node.path("steps")) {
      recipe.addStep(stepNode.asText());
    }
    return recipe;
  }

  /**
   * Writes food items and their amounts to a JSON array. Each item is written as an object with
   * {@code name}, {@code quantity} and {@code unit}, and the unit is written as its enum name.
   *
   * @param array the JSON array to add the items to
   * @param amounts the food items mapped to their amounts
   */

  private static void writeAmounts(ArrayNode array, Map<FoodItem, Amount> amounts) {
    for (Map.Entry<FoodItem, Amount> entry : amounts.entrySet()) {
      ObjectNode itemNode = array.addObject();
      itemNode.put("name", entry.getKey().getName());
      itemNode.put("quantity", entry.getValue().quantity());
      itemNode.put("unit", entry.getValue().unit().name());
    }
  }

  /**
   * Reads an amount from the {@code quantity} and {@code unit} fields of a JSON object.
   *
   * @param node the JSON object to read from
   * @return the amount described by the object
   * @throws IllegalArgumentException if {@code quantity} is not a number, or if {@code unit} is
   *     missing or not a known unit
   */

  private static Amount readAmount(JsonNode node) {
    JsonNode quantity = node.path("quantity");
    if (!quantity.isNumber()) {
      throw new IllegalArgumentException("mangler gyldig mengde");
    }
    return new Amount(quantity.asDouble(), Unit.parseUnit(requiredText(node, "unit")));
  }

  /**
   * Reads a text field that must be present.
   *
   * @param node the JSON object to read from
   * @param field the name of the field
   * @return the field's value
   * @throws IllegalArgumentException if the field is missing or does not hold text
   */

  private static String requiredText(JsonNode node, String field) {
    JsonNode value = node.path(field);
    if (!value.isTextual()) {
      throw new IllegalArgumentException("mangler feltet " + field);
    }
    return value.asText();
  }
}
