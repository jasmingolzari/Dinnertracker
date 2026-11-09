package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/**
 * Tests login validation, the calls made to the REST API, how the loaded data is stored in {@link
 * App}, and navigation through the FXML form. The REST client is replaced by a Mockito mock, so no
 * server needs to run.
 */
class UserLogInPageControllerTest {

  /** Mocked REST client, so no requests are sent to a real server. */
  private DinnertrackerRestClient restClient;

  /** The window that shows the login form during the test. */
  private Stage stage;

  /** The root node of the loaded login form. */
  private Parent loginRoot;

  /** Text field where the username is typed. */
  private TextField usernameTextField;

  /** The button that logs in. */
  private Button loginButton;

  /** The logged-in user before the test, restored afterwards. */
  private User originalUser;

  /** The fridge in {@link App} before the test, restored afterwards. */
  private Fridge originalFridge;

  /** The recipe book in {@link App} before the test, restored afterwards. */
  private RecipeBook originalRecipeBook;

  /** User logged in when the test starts, used to check that a failed login changes nothing. */
  private User initialUser;

  /** The fridge of {@link #initialUser}, with milk in it. */
  private Fridge initialFridge;

  /** The recipe book of {@link #initialUser}, with one recipe in it. */
  private RecipeBook initialRecipeBook;

  /** Starts JavaFX and keeps it running between tests when their stages are closed. */
  @BeforeAll
  static void startToolkit() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException exception) {
      // Another controller test has already started JavaFX.
    }
    Platform.setImplicitExit(false);
  }

  /**
   * Loads the real login form with a mocked REST client. By default the mock answers like a server
   * where nobody is registered yet: no user, an empty fridge and no recipes.
   */
  @BeforeEach
  void setUp() throws Exception {
    restClient = mock(DinnertrackerRestClient.class);
    when(restClient.getFridge(any(User.class)))
        .thenAnswer(invocation -> new Fridge(invocation.getArgument(0)));
    when(restClient.getRecipeBook(any(User.class)))
        .thenAnswer(invocation -> new RecipeBook(invocation.getArgument(0)));

    runOnFxThread(
        () -> {
          originalUser = App.getCurrentUser();
          originalFridge = App.getFridge();
          originalRecipeBook = App.getRecipeBook();
          initialUser = new User("previous-user");
          initialFridge = new Fridge(initialUser);
          initialFridge.addFoodItem(new FoodItem("Milk"), new Amount(1.0, Unit.PIECE));
          initialRecipeBook = new RecipeBook(initialUser);
          initialRecipeBook.addRecipe(new Recipe("Previous recipe", initialUser));
          App.setCurrentUser(initialUser);
          App.setFridge(initialFridge);
          App.setRecipeBook(initialRecipeBook);

          stage = new Stage();
          FXMLLoader loader =
              new FXMLLoader(UserLogInPageController.class.getResource("UserLogInPage.fxml"));
          loader.setControllerFactory(type -> new UserLogInPageController(restClient));
          loginRoot = loader.load();
          stage.setScene(new Scene(loginRoot));
          usernameTextField = (TextField) loginRoot.lookup("#usernameTextField");
          loginButton = (Button) loginRoot.lookup(".button");
          assertNotNull(usernameTextField);
          assertNotNull(loginButton);
        });
  }

  /** Closes the test stage and restores all shared application state. */
  @AfterEach
  void tearDown() throws Exception {
    runOnFxThread(
        () -> {
          if (stage != null) {
            stage.close();
          }
          App.setCurrentUser(originalUser);
          App.setFridge(originalFridge);
          App.setRecipeBook(originalRecipeBook);
        });
  }

  /** Verifies that FXML can also create the controller through its public default constructor. */
  @Test
  void fxmlShouldLoadWithDefaultController() throws Exception {
    runOnFxThread(
        () -> {
          FXMLLoader loader =
              new FXMLLoader(UserLogInPageController.class.getResource("UserLogInPage.fxml"));

          Parent root = loader.load();

          assertInstanceOf(UserLogInPageController.class, loader.getController());
          assertNotNull(root.lookup("#usernameTextField"));
          Button button = (Button) root.lookup(".button");
          assertNotNull(button);
          assertNotNull(button.getOnAction());
          assertTrue(button.isDefaultButton());
        });
  }

  /**
   * Missing usernames are rejected without contacting the server or changing any state.
   *
   * @param username the missing or blank username to type in
   */
  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t"})
  void loginShouldRejectMissingUsername(String username) throws Exception {
    runOnFxThread(
        () -> {
          usernameTextField.setText(username);

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            loginButton.fire();

            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "Login Error",
                        "Brukernavn påkrevd",
                        "Vennligst skriv inn et gyldig brukernavn."));
            alerts.verifyNoMoreInteractions();
          }
          verifyNoInteractions(restClient);
          assertLoginStateUnchanged();
        });
  }

  /**
   * New users are registered on the server with a trimmed name and get their own empty data.
   *
   * @param username the username to type in, with or without surrounding spaces
   */
  @ParameterizedTest
  @ValueSource(strings = {"new-user", "  new-user  "})
  void loginShouldRegisterNewUserAndOpenHomePage(String username) throws Exception {
    runOnFxThread(
        () -> {
          usernameTextField.setText(username);
          final Scene loginScene = stage.getScene();

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            loginButton.fire();

            alerts.verify(
                () ->
                    ShowAlertMessage.showInfoAlert(
                        "Bruker opprettet", null, "Ny bruker opprettet. Velkommen, new-user!"));
            alerts.verifyNoMoreInteractions();
          }
          verify(restClient).getUser("new-user");
          ArgumentCaptor<User> created = ArgumentCaptor.forClass(User.class);
          verify(restClient).createUser(created.capture());
          assertEquals("new-user", created.getValue().getUserName());

          User user = App.getCurrentUser();
          assertSame(created.getValue(), user);
          assertSame(user, App.getFridge().getUserName());
          assertSame(user, App.getRecipeBook().getUserName());
          assertTrue(App.getFridge().getFoodItems().isEmpty());
          assertTrue(App.getRecipeBook().getRecipes().isEmpty());
          assertSame(loginScene, stage.getScene());
          assertHomePageIsOpen();
        });
  }

  /** Returning users get their fridge and recipes from the server and are not registered again. */
  @Test
  void loginShouldLoadExistingUserDataWithoutRegisteringAgain() throws Exception {
    User user = new User("existing-user");
    FoodItem ingredient = new FoodItem("Tomato");
    Amount amount = new Amount(3.0, Unit.PIECE);
    Fridge fridge = new Fridge(user);
    fridge.addFoodItem(ingredient, amount);
    Recipe recipe = new Recipe("Tomato soup", user);
    recipe.addIngredient(ingredient, amount);
    recipe.addStep("Cook the tomatoes.");
    RecipeBook recipeBook = new RecipeBook(user);
    recipeBook.addRecipe(recipe);

    when(restClient.getUser("existing-user")).thenReturn(user);
    when(restClient.getFridge(user)).thenReturn(fridge);
    when(restClient.getRecipeBook(user)).thenReturn(recipeBook);

    runOnFxThread(
        () -> {
          usernameTextField.setText("  existing-user  ");
          final Scene loginScene = stage.getScene();

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            loginButton.fire();

            alerts.verify(
                () ->
                    ShowAlertMessage.showInfoAlert(
                        "Login Suksessfull", null, "Velkommen tilbake, existing-user!"));
            alerts.verifyNoMoreInteractions();
          }
          verify(restClient, never()).createUser(any(User.class));
          assertSame(user, App.getCurrentUser());
          assertSame(fridge, App.getFridge());
          assertSame(recipeBook, App.getRecipeBook());
          assertEquals(Map.of(ingredient, amount), App.getFridge().getFoodItems());
          Recipe loadedRecipe = App.getRecipeBook().getRecipeByName("Tomato soup");
          assertEquals(List.of("Cook the tomatoes."), loadedRecipe.getSteps());
          assertSame(loginScene, stage.getScene());
          assertHomePageIsOpen();
        });
  }

  /**
   * If any request fails (for example because the server is not running), an error is shown, the
   * login page stays open and the previous user and data are kept.
   *
   * @param failingCall the name of the REST client method that fails
   */
  @ParameterizedTest
  @ValueSource(strings = {"getUser", "getFridge", "getRecipeBook", "createUser"})
  void loginShouldHandleServerFailure(String failingCall) throws Exception {
    IOException failure = new IOException("Får ikke kontakt med serveren");
    switch (failingCall) {
      case "getUser" -> when(restClient.getUser(anyString())).thenThrow(failure);
      case "getFridge" -> when(restClient.getFridge(any(User.class))).thenThrow(failure);
      case "getRecipeBook" -> when(restClient.getRecipeBook(any(User.class))).thenThrow(failure);
      default -> doThrow(failure).when(restClient).createUser(any(User.class));
    }

    runOnFxThread(
        () -> {
          usernameTextField.setText("new-user");

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            loginButton.fire();

            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        eq("Feil"), eq("Kunne ikke logge inn"), startsWith("En feil oppstod: ")));
            alerts.verifyNoMoreInteractions();
          }
          assertLoginStateUnchanged();
        });
  }

  /**
   * Checks that the login page is still shown and that the user, fridge and recipe book in {@link
   * App} are the same as before the test.
   */
  private void assertLoginStateUnchanged() {
    assertSame(loginRoot, stage.getScene().getRoot());
    assertSame(initialUser, App.getCurrentUser());
    assertSame(initialFridge, App.getFridge());
    assertSame(initialRecipeBook, App.getRecipeBook());
  }

  /**
   * Checks that the login form has been replaced by the home page, which has the fridge and recipe
   * buttons and no username field.
   */
  private void assertHomePageIsOpen() {
    Parent root = stage.getScene().getRoot();
    assertNotSame(loginRoot, root);
    assertNotNull(root.lookup("#fridgeButton"));
    assertNotNull(root.lookup("#recipeButton"));
    assertNull(root.lookup("#usernameTextField"));
  }

  /**
   * Runs JavaFX work on its application thread and waits for it to finish.
   *
   * @param action the work to run
   * @throws Exception if the work fails or does not finish within ten seconds
   */
  private static void runOnFxThread(ThrowingRunnable action) throws Exception {
    FutureTask<Void> task =
        new FutureTask<>(
            () -> {
              action.run();
              return null;
            });
    Platform.runLater(task);
    task.get(10, TimeUnit.SECONDS);
  }

  /** A piece of work that, unlike {@link Runnable}, may throw a checked exception. */
  @FunctionalInterface
  private interface ThrowingRunnable {
    /**
     * Runs the work.
     *
     * @throws Exception if the work fails
     */
    void run() throws Exception;
  }
}
