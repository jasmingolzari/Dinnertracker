package no.ntnu.it1901.gr2631.dinnertracker.ui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.ui.HomePageController;

class HomePageControllerTest {

  @BeforeAll
  static void initJavaFx() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException e) {

    }
  }

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

    assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout på FX-tråden");
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

          // Check something unique from FridgePage.fxml here
          // assertNotNull(stage.getScene().lookup("#someFridgeId"));

          stage.close();
        });
  }

  @Test
  void recipeButtonOpensRecipePage() throws Throwable {
    runOnFx(
        () -> {
          Parent root = FXMLLoader.load(HomePageController.class.getResource("HomePage.fxml"));

          Stage stage = new Stage();
          try {
          stage.setScene(new Scene(root));
          stage.show();

          Button recipeButton = (Button) root.lookup("#recipeButton");

          assertNotNull(recipeButton);

          recipeButton.fire();
          assertNotNull(stage.getScene().lookup("#nameField"));
        } finally {
          stage.close(); }
        });
  }
}
