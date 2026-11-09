package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests the REST client without a running server. The {@link HttpClient} is a Mockito mock, so the
 * tests check both which HTTP requests are sent and how the server's answers are interpreted.
 */
class DinnertrackerRestClientTest {

  /** Used to read the JSON bodies of the requests the client sends. */
  private static final ObjectMapper JSON = new ObjectMapper();

  /** Mocked HTTP client that records the requests instead of sending them. */
  private HttpClient httpClient;

  /** Mocked response that the HTTP client returns for every request. */
  private HttpResponse<String> response;

  /** The client being tested, pointed at the mocked HTTP client. */
  private DinnertrackerRestClient client;

  /** Test user whose name contains a space, to check that usernames are URL-encoded. */
  private final User user = new User("ola nordmann");

  /** Creates a client that sends every request to a mocked HTTP client. */
  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() throws Exception {
    httpClient = mock(HttpClient.class);
    response = mock(HttpResponse.class);
    doReturn(response).when(httpClient).send(any(HttpRequest.class), any());
    client = new DinnertrackerRestClient(URI.create("http://localhost:7070/"), httpClient);
  }

  /** Clears the interrupt flag on the current thread after each test. */
  @AfterEach
  void clearInterruptFlag() {
    // A test below interrupts the current thread on purpose; do not leak that to other tests.
    Thread.interrupted();
  }

  /**
   * Sets the status code and body that the mocked server answers with.
   *
   * @param status the HTTP status code to return
   * @param body the response body to return
   */
  private void answer(int status, String body) {
    when(response.statusCode()).thenReturn(status);
    when(response.body()).thenReturn(body);
  }

  /**
   * Returns the request that the client sent, and checks that exactly one request was sent.
   *
   * @return the request passed to the mocked HTTP client
   * @throws Exception if the request cannot be captured
   */
  private HttpRequest sentRequest() throws Exception {
    ArgumentCaptor<HttpRequest> request = ArgumentCaptor.forClass(HttpRequest.class);
    verify(httpClient).send(request.capture(), any());
    return request.getValue();
  }

  /**
   * Reads the body of a request by subscribing to its body publisher.
   *
   * @param request the request whose body should be read
   * @return the body parsed as JSON
   * @throws Exception if the request has no body, the body cannot be read within five seconds, or
   *     the body is not valid JSON
   */
  private static JsonNode bodyOf(HttpRequest request) throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    CompletableFuture<Void> done = new CompletableFuture<>();
    request
        .bodyPublisher()
        .orElseThrow()
        .subscribe(
            new Flow.Subscriber<ByteBuffer>() {
              @Override
              public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
              }

              @Override
              public void onNext(ByteBuffer item) {
                byte[] chunk = new byte[item.remaining()];
                item.get(chunk);
                bytes.write(chunk, 0, chunk.length);
              }

              @Override
              public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
              }

              @Override
              public void onComplete() {
                done.complete(null);
              }
            });
    done.get(5, TimeUnit.SECONDS);
    return JSON.readTree(bytes.toString(StandardCharsets.UTF_8));
  }

  /**
   * Verifies that the default constructor can be created when the server address is set as a
   * system property. The previous value of the property is restored afterwards.
   */
  @Test
  void defaultConstructorShouldReadServerAddressProperty() {
    String previous = System.getProperty(DinnertrackerRestClient.BASE_URL_PROPERTY);
    try {
      System.setProperty(DinnertrackerRestClient.BASE_URL_PROPERTY, "http://example.test:1234");
      assertDoesNotThrow(() -> new DinnertrackerRestClient());
    } finally {
      if (previous == null) {
        System.clearProperty(DinnertrackerRestClient.BASE_URL_PROPERTY);
      } else {
        System.setProperty(DinnertrackerRestClient.BASE_URL_PROPERTY, previous);
      }
    }
  }

  /**
   * Verifies that a registered user is returned, and that the request is a GET with the username
   * URL-encoded in the query.
   */
  @Test
  void getUserShouldReturnRegisteredUser() throws Exception {
    answer(200, "ola nordmann");

    User found = client.getUser("ola nordmann");

    assertEquals("ola nordmann", found.getUserName());
    HttpRequest request = sentRequest();
    assertEquals("GET", request.method());
    assertEquals(URI.create("http://localhost:7070/users?userName=ola%20nordmann"), request.uri());
  }

  /** Verifies that {@code null} is returned when the server answers 404 Not Found. */
  @Test
  void getUserShouldReturnNullForUnknownUser() throws Exception {
    answer(404, "User not found");

    assertNull(client.getUser("ukjent"));
  }

  /** Verifies that creating a user sends a POST to {@code /users} with the username as JSON. */
  @Test
  void createUserShouldPostUserName() throws Exception {
    answer(201, "User created successfully");

    client.createUser(user);

    HttpRequest request = sentRequest();
    assertEquals("POST", request.method());
    assertEquals(URI.create("http://localhost:7070/users"), request.uri());
    assertEquals("ola nordmann", bodyOf(request).get("userName").asText());
  }

  /**
   * Verifies that an {@link IOException} containing the status code is thrown when the server
   * answers 409 Conflict because the user already exists.
   */
  @Test
  void createUserShouldFailWhenUserAlreadyExists() {
    answer(409, "User already exists");

    IOException error = assertThrows(IOException.class, () -> client.createUser(user));
    assertTrue(error.getMessage().contains("409"));
  }

  /**
   * Verifies that the food items in the server's answer are read into a fridge linked to the given
   * user. Units can be given both as enum names and as symbols.
   */
  @Test
  void getFridgeShouldReadFoodItems() throws Exception {
    answer(
        200,
        """
        {"userName": "ola nordmann",
         "foodItems": [
           {"name": "Melk", "quantity": 1.5, "unit": "LITRE"},
           {"name": "Egg", "quantity": 6, "unit": "stk"}
         ]}
        """);

    Fridge fridge = client.getFridge(user);

    assertSame(user, fridge.getUserName());
    assertEquals(
        Map.of(
            new FoodItem("Melk"), new Amount(1.5, Unit.LITRE),
            new FoodItem("Egg"), new Amount(6, Unit.PIECE)),
        fridge.getFoodItems());
    assertEquals(
        URI.create("http://localhost:7070/fridges?userName=ola%20nordmann"), sentRequest().uri());
  }

  /** Verifies that an {@link IOException} is thrown when a food item is missing its quantity. */
  @Test
  void getFridgeShouldRejectInvalidData() {
    answer(200, "{\"foodItems\": [{\"name\": \"Melk\", \"unit\": \"LITRE\"}]}");

    assertThrows(IOException.class, () -> client.getFridge(user));
  }

  /** Verifies that an {@link IOException} is thrown when a food item has an unknown unit. */
  @Test
  void getFridgeShouldRejectUnknownUnit() {
    answer(200, "{\"foodItems\": [{\"name\": \"Melk\", \"quantity\": 1, \"unit\": \"kopp\"}]}");

    assertThrows(IOException.class, () -> client.getFridge(user));
  }

  /**
   * Verifies that updating a fridge sends a PUT with the owner's username and every food item,
   * with units written as enum names.
   */
  @Test
  void updateFridgeShouldPutAllFoodItems() throws Exception {
    answer(200, "Fridge updated successfully");
    Fridge fridge = new Fridge(user);
    fridge.addFoodItem(new FoodItem("Ost"), new Amount(500, Unit.GRAM));

    client.updateFridge(fridge);

    HttpRequest request = sentRequest();
    assertEquals("PUT", request.method());
    assertEquals(
        URI.create("http://localhost:7070/fridges?userName=ola%20nordmann"), request.uri());
    JsonNode body = bodyOf(request);
    assertEquals("ola nordmann", body.get("userName").asText());
    JsonNode item = body.get("foodItems").get(0);
    assertEquals("Ost", item.get("name").asText());
    assertEquals(500.0, item.get("quantity").asDouble());
    assertEquals("GRAM", item.get("unit").asText());
  }

  /** Verifies that an {@link IOException} is thrown when the server rejects the fridge. */
  @Test
  void updateFridgeShouldFailOnBadRequest() {
    answer(400, "Invalid food item");

    assertThrows(IOException.class, () -> client.updateFridge(new Fridge(user)));
  }

  /**
   * Verifies that recipes from all users are read into one recipe book. The logged-in user's own
   * recipes are linked to the given user object, and other users' recipes keep their owner's name.
   */
  @Test
  void getRecipeBookShouldReadSharedRecipesFromAllUsers() throws Exception {
    answer(
        200,
        """
        [{"userName": "ola nordmann", "name": "Omelett",
          "ingredients": [{"name": "Egg", "quantity": 3, "unit": "PIECE"}],
          "steps": ["Visp", "Stek"]},
         {"userName": "kari", "name": "Pizza", "ingredients": [], "steps": ["Bak"]}]
        """);

    RecipeBook recipeBook = client.getRecipeBook(user);

    assertEquals(URI.create("http://localhost:7070/recipes/all"), sentRequest().uri());
    assertSame(user, recipeBook.getUserName());
    assertEquals(2, recipeBook.getRecipes().size());

    Recipe own = recipeBook.getRecipeByName("Omelett");
    assertSame(user, own.getUserName());
    assertEquals(Map.of(new FoodItem("Egg"), new Amount(3, Unit.PIECE)), own.getIngredients());
    assertEquals(List.of("Visp", "Stek"), own.getSteps());

    Recipe shared = recipeBook.getRecipeByName("Pizza");
    assertEquals("kari", shared.getUserName().getUserName());
    assertEquals(List.of("Bak"), shared.getSteps());
  }

  /** Verifies that an {@link IOException} is thrown when a recipe from the server has no name. */
  @Test
  void getRecipeBookShouldRejectRecipeWithoutName() {
    answer(200, "[{\"userName\": \"kari\", \"ingredients\": [], \"steps\": []}]");

    assertThrows(IOException.class, () -> client.getRecipeBook(user));
  }

  /** Verifies that an {@link IOException} is thrown when the server answers with an error. */
  @Test
  void getRecipeBookShouldFailOnServerError() {
    answer(500, "Could not load recipes");

    assertThrows(IOException.class, () -> client.getRecipeBook(user));
  }

  /**
   * Verifies that creating a recipe sends a POST to {@code /recipes} with the owner, name,
   * ingredients and steps as JSON.
   */
  @Test
  void createRecipeShouldPostRecipe() throws Exception {
    answer(201, "Recipe created successfully");
    Recipe recipe =
        new Recipe(
            "Pannekaker",
            user,
            Map.of(new FoodItem("Mel"), new Amount(2.5, Unit.TABLESPOON)),
            List.of("Bland", "Stek"));

    client.createRecipe(recipe);

    HttpRequest request = sentRequest();
    assertEquals("POST", request.method());
    assertEquals(URI.create("http://localhost:7070/recipes"), request.uri());
    JsonNode body = bodyOf(request);
    assertEquals("ola nordmann", body.get("userName").asText());
    assertEquals("Pannekaker", body.get("name").asText());
    JsonNode ingredient = body.get("ingredients").get(0);
    assertEquals("Mel", ingredient.get("name").asText());
    assertEquals(2.5, ingredient.get("quantity").asDouble());
    assertEquals("TABLESPOON", ingredient.get("unit").asText());
    assertEquals("Bland", body.get("steps").get(0).asText());
    assertEquals("Stek", body.get("steps").get(1).asText());
  }

  /** Verifies that an {@link IOException} is thrown when the server rejects the recipe. */
  @Test
  void createRecipeShouldFailWhenRejected() {
    answer(400, "userName and name are required");

    assertThrows(IOException.class, () -> client.createRecipe(new Recipe("Tom", user)));
  }

  /**
   * Verifies that a failed connection gives an error message with the server address and a hint
   * to start the REST server.
   */
  @Test
  void unreachableServerShouldGiveHelpfulMessage() throws Exception {
    doThrow(new ConnectException()).when(httpClient).send(any(HttpRequest.class), any());

    IOException error = assertThrows(IOException.class, () -> client.getUser("ola"));

    assertTrue(error.getMessage().contains("http://localhost:7070"));
    assertTrue(error.getMessage().contains("REST-serveren"));
  }

  /**
   * Verifies that an interrupted request throws an {@link IOException} and sets the thread's
   * interrupt flag again.
   */
  @Test
  void interruptedRequestShouldKeepInterruptFlag() throws Exception {
    doThrow(new InterruptedException()).when(httpClient).send(any(HttpRequest.class), any());

    assertThrows(IOException.class, () -> client.getUser("ola"));

    assertTrue(Thread.currentThread().isInterrupted());
  }
}
