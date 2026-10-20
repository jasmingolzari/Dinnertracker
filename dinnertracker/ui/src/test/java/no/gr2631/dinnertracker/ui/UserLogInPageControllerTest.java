package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mockStatic;

import java.nio.file.Files;
import java.nio.file.Path;
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
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

/** Tests login validation, user persistence, data loading, and navigation through the FXML form. */
class UserLogInPageControllerTest {

  @TempDir Path tempDir;

  private Path usersPath;
  private Path fridgesPath;
  private Path recipesPath;
  private Stage stage;
  private Parent loginRoot;
  private TextField usernameTextField;
  private Button loginButton;
  private User originalUser;
  private Fridge originalFridge;
  private RecipeBook originalRecipeBook;
  private User initialUser;
  private Fridge initialFridge;
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
   * Loads the real login form with temporary persistence paths and an existing application user.
   */
  @BeforeEach
  void setUp() throws Exception {
    usersPath = tempDir.resolve("users.json");
    fridgesPath = tempDir.resolve("fridges.json");
    recipesPath = tempDir.resolve("recipes.json");

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
          loadLoginPage(usersPath);
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

  /** Missing usernames leave the active user, loaded data, and login form unchanged. */
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
          assertSame(loginRoot, stage.getScene().getRoot());
          assertSame(initialUser, App.getCurrentUser());
          assertSame(initialFridge, App.getFridge());
          assertSame(initialRecipeBook, App.getRecipeBook());
          assertFalse(Files.exists(usersPath));
          assertFalse(Files.exists(fridgesPath));
          assertFalse(Files.exists(recipesPath));
        });
  }

  /** New users are registered with a trimmed name and receive their own empty application data. */
  @ParameterizedTest
  @ValueSource(strings = {"new-user", "  new-user  "})
  void loginShouldRegisterNewUserAndOpenHomePage(String username) throws Exception {
    runOnFxThread(
        () -> {
          usernameTextField.setText(username);
          Scene loginScene = stage.getScene();

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            loginButton.fire();

            alerts.verify(
                () ->
                    ShowAlertMessage.showInfoAlert(
                        "Bruker opprettet", null, "Ny bruker opprettet. Velkommen, new-user!"));
            alerts.verifyNoMoreInteractions();
          }
          User user = App.getCurrentUser();
          assertEquals("new-user", user.getUserName());
          assertEquals("new-user", App.usersFileHandler.load(usersPath, user).getUserName());
          assertNull(App.usersFileHandler.load(usersPath, new User("  new-user  ")));
          assertSame(user, App.getFridge().getUserName());
          assertSame(user, App.getRecipeBook().getUserName());
          assertTrue(App.getFridge().getFoodItems().isEmpty());
          assertTrue(App.getRecipeBook().getRecipes().isEmpty());
          assertFalse(Files.exists(fridgesPath));
          assertFalse(Files.exists(recipesPath));
          assertSame(loginScene, stage.getScene());
          assertHomePageIsOpen();
        });
  }

  /** Registering another user preserves users that were already registered. */
  @Test
  void loginShouldPreserveOtherRegisteredUsers() throws Exception {
    App.usersFileHandler.save(usersPath, new User("existing-user"));

    runOnFxThread(
        () -> {
          usernameTextField.setText("new-user");
          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            loginButton.fire();
            alerts.verify(
                () ->
                    ShowAlertMessage.showInfoAlert(
                        "Bruker opprettet", null, "Ny bruker opprettet. Velkommen, new-user!"));
            alerts.verifyNoMoreInteractions();
          }

          assertNotNull(App.usersFileHandler.load(usersPath, new User("existing-user")));
          assertNotNull(App.usersFileHandler.load(usersPath, new User("new-user")));
          assertHomePageIsOpen();
        });
  }

  /** Returning users load their saved fridge and recipes without rewriting the user registry. */
  @Test
  void loginShouldLoadExistingUserDataWithoutRegisteringAgain() throws Exception {
    User user = new User("existing-user");
    App.usersFileHandler.save(usersPath, user);
    String registeredUsers = Files.readString(usersPath);
    Fridge fridge = new Fridge(user);
    FoodItem ingredient = new FoodItem("Tomato");
    Amount amount = new Amount(3.0, Unit.PIECE);
    fridge.addFoodItem(ingredient, amount);
    App.fridgeFileHandler.save(fridgesPath, fridge);
    Recipe recipe = new Recipe("Tomato soup", user);
    recipe.addIngredient(ingredient, amount);
    recipe.addStep("Cook the tomatoes.");
    RecipeBook recipeBook = new RecipeBook(user);
    recipeBook.addRecipe(recipe);
    App.recipeFileHandler.save(recipesPath, recipeBook);

    runOnFxThread(
        () -> {
          usernameTextField.setText("  existing-user  ");
          Scene loginScene = stage.getScene();

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            loginButton.fire();

            alerts.verify(
                () ->
                    ShowAlertMessage.showInfoAlert(
                        "Login Suksessfull", null, "Velkommen tilbake, existing-user!"));
            alerts.verifyNoMoreInteractions();
          }
          User loggedInUser = App.getCurrentUser();
          assertEquals("existing-user", loggedInUser.getUserName());
          assertSame(loggedInUser, App.getFridge().getUserName());
          assertSame(loggedInUser, App.getRecipeBook().getUserName());
          assertEquals(Map.of(ingredient, amount), App.getFridge().getFoodItems());
          assertEquals(1, App.getRecipeBook().getRecipes().size());
          Recipe loadedRecipe = App.getRecipeBook().getRecipeByName("Tomato soup");
          assertEquals("existing-user", loadedRecipe.getUserName().getUserName());
          assertEquals(Map.of(ingredient, amount), loadedRecipe.getIngredients());
          assertEquals(List.of("Cook the tomatoes."), loadedRecipe.getSteps());
          assertEquals(registeredUsers, Files.readString(usersPath));
          assertSame(loginScene, stage.getScene());
          assertHomePageIsOpen();
        });
  }

  /** An unreadable data file shows an error before registering a user or opening the home page. */
  @ParameterizedTest
  @ValueSource(strings = {"users", "fridges", "recipes"})
  void loginShouldHandleDataLoadFailure(String fileName) throws Exception {
    Path invalidFile = tempDir.resolve(fileName + ".json");
    Files.writeString(invalidFile, "[");

    runOnFxThread(
        () -> {
          usernameTextField.setText("new-user");

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            loginButton.fire();

            verifyLoginError(alerts);
          }
          assertSame(loginRoot, stage.getScene().getRoot());
          assertSame(initialUser, App.getCurrentUser());
          assertEquals("[", Files.readString(invalidFile));
          if (!invalidFile.equals(usersPath)) {
            assertFalse(Files.exists(usersPath));
          }
        });
  }

  /** A registration write failure leaves the login page open and reports the failure. */
  @Test
  void loginShouldHandleUserSaveFailure() throws Exception {
    Path missingParentUsersPath = tempDir.resolve("missing").resolve("users.json");

    runOnFxThread(
        () -> {
          loadLoginPage(missingParentUsersPath);
          usernameTextField.setText("new-user");

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            loginButton.fire();

            verifyLoginError(alerts);
          }
          assertSame(loginRoot, stage.getScene().getRoot());
          assertFalse(Files.exists(missingParentUsersPath));
        });
  }

  private void loadLoginPage(Path userFile) throws Exception {
    FXMLLoader loader =
        new FXMLLoader(UserLogInPageController.class.getResource("UserLogInPage.fxml"));
    loader.setControllerFactory(
        type -> new UserLogInPageController(userFile, fridgesPath, recipesPath));
    loginRoot = loader.load();
    stage.setScene(new Scene(loginRoot));
    usernameTextField = (TextField) loginRoot.lookup("#usernameTextField");
    loginButton = (Button) loginRoot.lookup(".button");
    assertNotNull(usernameTextField);
    assertNotNull(loginButton);
  }

  private void assertHomePageIsOpen() {
    Parent root = stage.getScene().getRoot();
    assertNotSame(loginRoot, root);
    assertNotNull(root.lookup("#fridgeButton"));
    assertNotNull(root.lookup("#recipeButton"));
    assertNull(root.lookup("#usernameTextField"));
  }

  private static void verifyLoginError(MockedStatic<ShowAlertMessage> alerts) {
    alerts.verify(
        () ->
            ShowAlertMessage.showErrorAlert(
                eq("Feil"), eq("Kunne ikke logge inn"), startsWith("En feil oppstod: ")));
    alerts.verifyNoMoreInteractions();
  }

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

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
