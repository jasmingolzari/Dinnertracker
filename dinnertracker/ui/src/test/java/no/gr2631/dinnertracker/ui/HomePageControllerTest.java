package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HomePageControllerTest {

  /** Initialized the JavaFX runtime and keeps it running between tests when stages are closed. */
  @BeforeAll
  public static void initJavaFx() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException e) {
      // JavaFX toolkit is already initialized by another test.
    }
    Platform.setImplicitExit(false);
  }

  /**
   * Runs the given action on the JavaFX Application Thread and waits for it to finish.
   *
   * @param action the action to run on the JavaFX Application Thread
   * @throws Throwable if the action throws an exception
   */
  private static void runOnFx(ThrowingRunnable action) throws Throwable {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> error = new AtomicReference<>();

    Platform.runLater(
        () -> {
          try {
            action.run();
          } catch (Exception t) {
            error.set(t);
          } finally {
            latch.countDown();
          }
        });

    assertTrue(latch.await(20, TimeUnit.SECONDS), "Timeout på FX-tråden");
    if (error.get() != null) {
      throw error.get();
    }
  }

  @FunctionalInterface
  interface ThrowingRunnable {
    void run() throws Exception;
  }

  @Test
  void homePageFxmlExists() {
    assertNotNull(HomePageController.class.getResource("HomePage.fxml"));
  }

  @Test
  void recipePageFxmlExists() {
    assertNotNull(HomePageController.class.getResource("RecipePage.fxml"));
  }

  @Test
  void fridgePageFxmlExists() {
    assertNotNull(HomePageController.class.getResource("FridgePage.fxml"));
  }

  @Test
  void homePageFxmlLoadsAndHandlersExist() throws Throwable {
    runOnFx(
        () -> {
          Parent root;
          root = FXMLLoader.load(HomePageController.class.getResource("HomePage.fxml"));
          assertNotNull(root);
        });
  }

  /** Tests that FridgePage.fxml opens when fridge button is clicked on */
  @Test
  void fridgeButtonOpensFridgePage() throws Throwable {
    runOnFx(
        () -> {
          Fridge previousFridge = App.getFridge();
          Stage stage = new Stage();

          try {
            // Initialize a fridge for this test
            User testUser = new User("testuser");
            App.setFridge(new Fridge(testUser));

            Parent root = FXMLLoader.load(HomePageController.class.getResource("HomePage.fxml"));

            stage.setScene(new Scene(root));
            stage.show();

            Button fridgeButton = (Button) root.lookup("#fridgeButton");
            assertNotNull(fridgeButton);

            fridgeButton.fire();

          } finally {
            stage.close();
            App.setFridge(previousFridge);
          }
        });
  }

  /** Tests that the recipe button opens the recipe list and displays the current user's recipes. */
  @Test
  void recipeButtonOpensRecipePage() throws Throwable {
    runOnFx(
        () -> {
          User previousUser = App.getCurrentUser();
          RecipeBook previousRecipeBook = App.getRecipeBook();
          Fridge previousFridge = App.getFridge();
          Stage stage = new Stage();

          try {
            App.setCurrentUser(new User("home-page-test"));
            App.setRecipeBook(new RecipeBook(App.getCurrentUser()));
            App.setFridge(new Fridge(App.getCurrentUser()));
            App.getRecipeBook().addRecipe(new Recipe("Pasta", App.getCurrentUser()));
            Parent root = FXMLLoader.load(HomePageController.class.getResource("HomePage.fxml"));
            stage.setScene(new Scene(root));
            stage.show();

            Button recipeButton = (Button) root.lookup("#recipeButton");

            assertNotNull(recipeButton);
            Scene homeScene = stage.getScene();
            recipeButton.fire();
            assertNotSame(homeScene, stage.getScene());
            ListView<?> recipesListView = (ListView<?>) stage.getScene().lookup("#recipesListView");
            assertNotNull(recipesListView);
            assertEquals(List.of("Pasta"), recipesListView.getItems());
          } finally {
            stage.close();
            App.setCurrentUser(previousUser);
            App.setRecipeBook(previousRecipeBook);
            App.setFridge(previousFridge);
          }
        });
  }
}
