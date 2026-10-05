package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.persistence.RecipeFileHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Tests for loading recipes and navigating from the recipe view page. */
public class RecipeViewPageControllerTest {

  private static boolean toolkitStarted;

  /** Starts the JavaFX toolkit so controls and FXML views can be created. */
  @BeforeAll
  public static void startToolkit() {
    try {
      Platform.startup(() -> {});
      Platform.setImplicitExit(false);
      toolkitStarted = true;
    } catch (IllegalStateException e) {
      Platform.setImplicitExit(false);
      toolkitStarted = true;
    } catch (Throwable e) {
      toolkitStarted = false;
    }
  }

  /** Skips JavaFX-dependent tests if the toolkit is unavailable. */
  private void requireToolkit() {
    assumeTrue(toolkitStarted, "JavaFX toolkit is not available");
  }

  /** Sets a private controller field for the test fixture. */
  private void setField(RecipeViewPageController controller, String name, Object value)
      throws Exception {
    Field field = RecipeViewPageController.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(controller, value);
  }

  /** Invokes a private controller method. */
  private void invoke(RecipeViewPageController controller, String name, Object argument)
      throws Exception {
    Method method = RecipeViewPageController.class.getDeclaredMethod(name, ActionEvent.class);
    method.setAccessible(true);
    method.invoke(controller, argument);
  }

  /** Runs an action on the JavaFX application thread and waits for it to finish. */
  private void runOnFxThread(ThrowingRunnable action) throws Exception {
    if (Platform.isFxApplicationThread()) {
      action.run();
      return;
    }

    CountDownLatch completed = new CountDownLatch(1);
    AtomicReference<Throwable> failure = new AtomicReference<>();
    Platform.runLater(
        () -> {
          try {
            action.run();
          } catch (Throwable throwable) {
            failure.set(throwable);
          } finally {
            completed.countDown();
          }
        });

    if (!completed.await(5, TimeUnit.SECONDS)) {
      throw new AssertionError("JavaFX action did not finish in time.");
    }
    if (failure.get() != null) {
      throw new AssertionError("JavaFX action failed.", failure.get());
    }
  }

  /** Loads recipes into the list view and verifies that only their names are displayed. */
  @Test
  public void initializeShouldDisplayRecipeNames() throws Exception {
    requireToolkit();
    RecipeViewPageController controller = new RecipeViewPageController();
    ListView<String> listView = new ListView<>();
    setField(controller, "recipesListView", listView);
    setField(
        controller,
        "filehandler",
        new RecipeFileHandler() {
          @Override
          public List<Recipe> loadAll(Path path) {
            return List.of(new Recipe("Pasta"), new Recipe("Suppe"));
          }
        });

    runOnFxThread(
        () -> {
          Method initialize = RecipeViewPageController.class.getDeclaredMethod("initialize");
          initialize.setAccessible(true);
          initialize.invoke(controller);
        });

    assertEquals(List.of("Pasta", "Suppe"), listView.getItems());
  }

  /** Handles a failed recipe load without propagating the checked exception. */
  @Test
  public void initializeShouldHandleLoadFailure() throws Exception {
    requireToolkit();
    RecipeViewPageController controller = new RecipeViewPageController();
    ListView<String> listView = new ListView<>();
    setField(controller, "recipesListView", listView);
    setField(
        controller,
        "filehandler",
        new RecipeFileHandler() {
          @Override
          public List<Recipe> loadAll(Path path) throws IOException {
            throw new IOException("test failure");
          }
        });

    assertDoesNotThrow(
        () ->
            runOnFxThread(
                () -> {
                  Method initialize =
                      RecipeViewPageController.class.getDeclaredMethod("initialize");
                  initialize.setAccessible(true);
                  initialize.invoke(controller);
                }));
  }

  /** Navigates to the recipe creation page. */
  @Test
  public void makeNewRecipeShouldReplaceSceneRoot() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          javafx.stage.Stage stage = new javafx.stage.Stage();
          Button source = new Button();
          stage.setScene(new Scene(new VBox(source)));
          RecipeViewPageController controller = new RecipeViewPageController();
          try {
            invoke(controller, "makeNewRecipe", new ActionEvent(source, source));
            assertEquals(VBox.class, stage.getScene().getRoot().getClass());
          } finally {
            stage.close();
          }
        });
  }

  /** Navigates back to the home page. */
  @Test
  public void backToHomePageShouldReplaceSceneRoot() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          javafx.stage.Stage stage = new javafx.stage.Stage();
          Button source = new Button();
          stage.setScene(new Scene(new VBox(source)));
          RecipeViewPageController controller = new RecipeViewPageController();
          try {
            invoke(controller, "backToHomePage", new ActionEvent(source, source));
            assertEquals(BorderPane.class, stage.getScene().getRoot().getClass());
          } finally {
            stage.close();
          }
        });
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
