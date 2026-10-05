package no.gr2631.dinnertracker.ui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

class HomePageControllerTest {

/** Initialized the JavaFX runtime and keeps it running
 * between tests when stages are closed.
 */
  @BeforeAll
  static void initJavaFx() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException e) {

    }
    Platform.setImplicitExit(false);
  }

  /** 
   * Runs the given action on the JavaFX Application Thread and 
   * waits for it to finish.
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
  /**
   * Tests that FridgePage.fxml opens when fridge button is clicked on
   */
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

  /**
   * Tests that RecipePage.fxml opens when recipe button is clicked on.
   */

@Test
void recipeButtonOpensRecipePage() throws Throwable {
  runOnFx(
      () -> {
        Parent root =
            FXMLLoader.load(
                HomePageController.class.getResource("HomePage.fxml"));

        Stage stage = new Stage();

        try {
          stage.setScene(new Scene(root));
          stage.show();

          Button recipeButton =
              (Button) root.lookup("#recipeButton");

          assertNotNull(recipeButton);
          Scene homeScene = stage.getScene();
          recipeButton.fire();
          assertNotSame(homeScene, stage.getScene());

        } finally {
          stage.close();
        }
      });
}
}
