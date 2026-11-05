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

  private static final ObjectMapper JSON = new ObjectMapper();

  private HttpClient httpClient;
  private HttpResponse<String> response;
  private DinnertrackerRestClient client;
  private final User user = new User("ola nordmann");

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() throws Exception {
    httpClient = mock(HttpClient.class);
    response = mock(HttpResponse.class);
    doReturn(response).when(httpClient).send(any(HttpRequest.class), any());
    client = new DinnertrackerRestClient(URI.create("http://localhost:7070/"), httpClient);
  }

  @AfterEach
  void clearInterruptFlag() {
    // A test below interrupts the current thread on purpose; do not leak that to other tests.
    Thread.interrupted();
  }

  private void answer(int status, String body) {
    when(response.statusCode()).thenReturn(status);
    when(response.body()).thenReturn(body);
  }

  private HttpRequest sentRequest() throws Exception {
    ArgumentCaptor<HttpRequest> request = ArgumentCaptor.forClass(HttpRequest.class);
    verify(httpClient).send(request.capture(), any());
    return request.getValue();
  }

  /** Reads the body of a request by subscribing to its body publisher. */
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

  @Test
  void getUserShouldReturnRegisteredUser() throws Exception {
    answer(200, "ola nordmann");

    User found = client.getUser("ola nordmann");

    assertEquals("ola nordmann", found.getUserName());
    HttpRequest request = sentRequest();
    assertEquals("GET", request.method());
    assertEquals(URI.create("http://localhost:7070/users?userName=ola%20nordmann"), request.uri());
  }

  @Test
  void getUserShouldReturnNullForUnknownUser() throws Exception {
    answer(404, "User not found");

    assertNull(client.getUser("ukjent"));
  }

  @Test
  void createUserShouldPostUserName() throws Exception {
    answer(201, "User created successfully");

    client.createUser(user);

    HttpRequest request = sentRequest();
    assertEquals("POST", request.method());
    assertEquals(URI.create("http://localhost:7070/users"), request.uri());
    assertEquals("ola nordmann", bodyOf(request).get("userName").asText());
  }

  @Test
  void createUserShouldFailWhenUserAlreadyExists() {
    answer(409, "User already exists");

    IOException error = assertThrows(IOException.class, () -> client.createUser(user));
    assertTrue(error.getMessage().contains("409"));
  }

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

  @Test
  void getFridgeShouldRejectInvalidData() {
    answer(200, "{\"foodItems\": [{\"name\": \"Melk\", \"unit\": \"LITRE\"}]}");

    assertThrows(IOException.class, () -> client.getFridge(user));
  }

  @Test
  void getFridgeShouldRejectUnknownUnit() {
    answer(200, "{\"foodItems\": [{\"name\": \"Melk\", \"quantity\": 1, \"unit\": \"kopp\"}]}");

    assertThrows(IOException.class, () -> client.getFridge(user));
  }

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

  @Test
  void updateFridgeShouldFailOnBadRequest() {
    answer(400, "Invalid food item");

    assertThrows(IOException.class, () -> client.updateFridge(new Fridge(user)));
  }

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

  @Test
  void getRecipeBookShouldRejectRecipeWithoutName() {
    answer(200, "[{\"userName\": \"kari\", \"ingredients\": [], \"steps\": []}]");

    assertThrows(IOException.class, () -> client.getRecipeBook(user));
  }

  @Test
  void getRecipeBookShouldFailOnServerError() {
    answer(500, "Could not load recipes");

    assertThrows(IOException.class, () -> client.getRecipeBook(user));
  }

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

  @Test
  void createRecipeShouldFailWhenRejected() {
    answer(400, "userName and name are required");

    assertThrows(IOException.class, () -> client.createRecipe(new Recipe("Tom", user)));
  }

  @Test
  void unreachableServerShouldGiveHelpfulMessage() throws Exception {
    doThrow(new ConnectException()).when(httpClient).send(any(HttpRequest.class), any());

    IOException error = assertThrows(IOException.class, () -> client.getUser("ola"));

    assertTrue(error.getMessage().contains("http://localhost:7070"));
    assertTrue(error.getMessage().contains("REST-serveren"));
  }

  @Test
  void interruptedRequestShouldKeepInterruptFlag() throws Exception {
    doThrow(new InterruptedException()).when(httpClient).send(any(HttpRequest.class), any());

    assertThrows(IOException.class, () -> client.getUser("ola"));

    assertTrue(Thread.currentThread().isInterrupted());
  }
}
