package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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

/** Tests for loading recipes and navigating from the recipe view page. */
public class RecipeViewPageControllerTest {

  private static boolean toolkitStarted;
  private User previousUser;
  private RecipeBook previousRecipeBook;
  private Fridge previousFridge;

  /** Starts the JavaFX toolkit so controls and FXML views can be created. */
  @BeforeAll
  public static void startToolkit() throws Exception {
    try {
      Platform.startup(() -> {});
      toolkitStarted = true;
    } catch (IllegalStateException e) {
      toolkitStarted = true;
    } catch (Throwable e) {
      toolkitStarted = false;
    }
    if (toolkitStarted) {
      runOnFxThread(() -> Platform.setImplicitExit(false));
    }
  }

  /** Gives each test a recipe book belonging to a known user. */
  @BeforeEach
  public void setUp() throws Exception {
    assumeTrue(toolkitStarted, "JavaFX toolkit is not available");
    runOnFxThread(
        () -> {
          previousUser = App.getCurrentUser();
          previousRecipeBook = App.getRecipeBook();
          previousFridge = App.getFridge();
          App.setCurrentUser(new User("recipe-view-test"));
          App.setRecipeBook(new RecipeBook(App.getCurrentUser()));
          App.setFridge(new Fridge(App.getCurrentUser()));
        });
  }

  /** Restores the application state changed by the test fixture. */
  @AfterEach
  public void tearDown() throws Exception {
    if (toolkitStarted) {
      runOnFxThread(
          () -> {
            App.setCurrentUser(previousUser);
            App.setRecipeBook(previousRecipeBook);
            App.setFridge(previousFridge);
          });
    }
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

  /** Initializes the controller after injecting its list view. */
  private void initialize(RecipeViewPageController controller) throws Exception {
    Method method = RecipeViewPageController.class.getDeclaredMethod("initialize");
    method.setAccessible(true);
    method.invoke(controller);
  }

  /** Runs an action on the JavaFX application thread and waits for it to finish. */
  private static void runOnFxThread(ThrowingRunnable action) throws Exception {
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
          } catch (Exception throwable) {
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
    runOnFxThread(
        () -> {
          App.getRecipeBook().addRecipe(new Recipe("Pasta", App.getCurrentUser()));
          App.getRecipeBook().addRecipe(new Recipe("Suppe", App.getCurrentUser()));
          RecipeViewPageController controller = new RecipeViewPageController();
          ListView<String> listView = new ListView<>();
          setField(controller, "recipesListView", listView);

          initialize(controller);

          assertEquals(List.of("Pasta", "Suppe"), listView.getItems());
        });
  }

  /** Clears old list entries when the current recipe book is empty. */
  @Test
  public void initializeShouldClearListForEmptyRecipeBook() throws Exception {
    runOnFxThread(
        () -> {
          RecipeViewPageController controller = new RecipeViewPageController();
          ListView<String> listView = new ListView<>();
          listView.getItems().add("Old recipe");
          setField(controller, "recipesListView", listView);

          initialize(controller);

          assertEquals(List.of(), listView.getItems());
        });
  }

  /** Refreshes the displayed recipes without duplicating existing entries. */
  @Test
  public void initializeShouldRefreshRecipeNames() throws Exception {
    runOnFxThread(
        () -> {
          App.getRecipeBook().addRecipe(new Recipe("Pasta", App.getCurrentUser()));
          RecipeViewPageController controller = new RecipeViewPageController();
          ListView<String> listView = new ListView<>();
          setField(controller, "recipesListView", listView);
          initialize(controller);

          App.getRecipeBook().addRecipe(new Recipe("Suppe", App.getCurrentUser()));
          initialize(controller);

          assertEquals(List.of("Pasta", "Suppe"), listView.getItems());
        });
  }

  /** Sorts fully makeable recipes before recipes with ingredients still missing. */
  @Test
  public void initializeShouldSortRecipesByIngredientsLeft() throws Exception {
    runOnFxThread(
        () -> {
          FoodItem pasta = new FoodItem("Pasta");
          FoodItem tomato = new FoodItem("Tomato");
          FoodItem cheese = new FoodItem("Cheese");
          App.getFridge().addFoodItem(pasta, new Amount(1, Unit.PIECE));
          App.getFridge().addFoodItem(tomato, new Amount(1, Unit.PIECE));
          App.getFridge().addFoodItem(cheese, new Amount(1, Unit.PIECE));

          Recipe partiallyAvailable =
              new Recipe(
                  "Partially available",
                  App.getCurrentUser(),
                  java.util.Map.of(
                      pasta,
                      new Amount(1, Unit.PIECE),
                      tomato,
                      new Amount(1, Unit.PIECE),
                      new FoodItem("Missing"),
                      new Amount(1, Unit.PIECE),
                      new FoodItem("Also missing"),
                      new Amount(1, Unit.PIECE),
                      new FoodItem("Still missing"),
                      new Amount(1, Unit.PIECE)),
                  List.of());
          Recipe fullyAvailable =
              new Recipe(
                  "Fully available",
                  App.getCurrentUser(),
                  java.util.Map.of(
                      pasta, new Amount(1, Unit.PIECE),
                      cheese, new Amount(1, Unit.PIECE)),
                  List.of());
          App.getRecipeBook().addRecipe(partiallyAvailable);
          App.getRecipeBook().addRecipe(fullyAvailable);

          RecipeViewPageController controller = new RecipeViewPageController();
          ListView<String> listView = new ListView<>();
          setField(controller, "recipesListView", listView);

          initialize(controller);

          assertEquals(List.of("Fully available", "Partially available"), listView.getItems());
        });
  }

  /** Refreshes recipe availability after a food item is added to the fridge. */
  @Test
  public void fridgeChangesShouldRefreshRecipeAvailability() throws Exception {
    runOnFxThread(
        () -> {
          FoodItem pasta = new FoodItem("Pasta");
          FoodItem missing = new FoodItem("Missing");
          Recipe availableRecipe =
              new Recipe(
                  "Available recipe",
                  App.getCurrentUser(),
                  java.util.Map.of(pasta, new Amount(1, Unit.PIECE)),
                  List.of());
          Recipe missingRecipe =
              new Recipe(
                  "Missing recipe",
                  App.getCurrentUser(),
                  java.util.Map.of(missing, new Amount(1, Unit.PIECE)),
                  List.of());
          App.getRecipeBook().addRecipe(availableRecipe);
          App.getRecipeBook().addRecipe(missingRecipe);

          RecipeViewPageController controller = new RecipeViewPageController();
          ListView<String> listView = new ListView<>();
          setField(controller, "recipesListView", listView);
          initialize(controller);

          App.getFridge().addFoodItem(missing, new Amount(1, Unit.PIECE));
          App.notifyFridgeChanged();

          assertEquals(List.of("Missing recipe", "Available recipe"), listView.getItems());
        });
  }

  /** Navigates to the recipe creation page. */
  @Test
  public void makeNewRecipeShouldReplaceSceneRoot() throws Exception {
    runOnFxThread(
        () -> {
          Stage stage = new Stage();
          try {
            Button source = new Button();
            stage.setScene(new Scene(new VBox(source)));
            RecipeViewPageController controller = new RecipeViewPageController();
            invoke(controller, "makeNewRecipe", new ActionEvent(source, source));
            assertEquals(ScrollPane.class, stage.getScene().getRoot().getClass());
          } finally {
            stage.close();
          }
        });
  }

  /** Navigates back to the home page. */
  @Test
  public void backToHomePageShouldReplaceSceneRoot() throws Exception {
    runOnFxThread(
        () -> {
          Stage stage = new Stage();
          try {
            Button source = new Button();
            stage.setScene(new Scene(new VBox(source)));
            RecipeViewPageController controller = new RecipeViewPageController();
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
