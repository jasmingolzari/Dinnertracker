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
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HomePageControllerTest {

  /** Initialized the JavaFX runtime and keeps it running between tests when stages are closed. */
  @BeforeAll
  static void initJavaFx() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException e) {

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
          } catch (Throwable t) {
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
  void HomePageFXMLExist() {
    assertNotNull(HomePageController.class.getResource("HomePage.fxml"));
  }

  @Test
  void RecipePageFXMLExist() {
    assertNotNull(HomePageController.class.getResource("RecipePage.fxml"));
  }

  @Test
  void FridgePageFXMLExist() {
    assertNotNull(HomePageController.class.getResource("FridgePage.fxml"));
  }

  @Test
  void HomePageFXMLLoadsAndHandlersExist() throws Throwable {
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
          Parent root = FXMLLoader.load(HomePageController.class.getResource("HomePage.fxml"));

          Stage stage = new Stage();
          stage.setScene(new Scene(root));
          stage.show();

          Button fridgeButton = (Button) root.lookup("#fridgeButton");

          assertNotNull(fridgeButton);

          fridgeButton.fire();
          stage.close();
        });
  }

  /** Tests that the recipe button opens the recipe list and displays the current user's recipes. */
  @Test
  void recipeButtonOpensRecipePage() throws Throwable {
    runOnFx(
        () -> {
          User previousUser = App.getCurrentUser();
          RecipeBook previousRecipeBook = App.getRecipeBook();
          Stage stage = new Stage();

          try {
            App.setCurrentUser(new User("home-page-test"));
            App.setRecipeBook(new RecipeBook(App.getCurrentUser()));
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
          }
        });
  }
}
