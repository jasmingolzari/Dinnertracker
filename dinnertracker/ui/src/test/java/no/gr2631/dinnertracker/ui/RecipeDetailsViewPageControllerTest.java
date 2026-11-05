package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
import org.mockito.MockedStatic;

/** Tests for RecipeDetailsViewPageController. */
class RecipeDetailsViewPageControllerTest {

  private Fridge previousFridge;
  private RecipeBook previousRecipeBook;

  private static void runOnFxThread(ThrowingRunnable action) throws Exception {
    if (Platform.isFxApplicationThread()) {
      action.run();
      return;
    }

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

    if (!latch.await(5, TimeUnit.SECONDS)) {
      throw new AssertionError("JavaFX operation timed out");
    }

    if (error.get() != null) {
      throw new AssertionError("JavaFX operation failed", error.get());
    }
  }

  @BeforeAll
  static void initializeJavaFx() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException e) {
      // JavaFX toolkit is already initialized.
    }
  }

  @BeforeEach
  void rememberAppState() {
    previousFridge = App.getFridge();
    previousRecipeBook = App.getRecipeBook();
  }

  @AfterEach
  void restoreAppState() {
    App.setFridge(previousFridge);
    App.setRecipeBook(previousRecipeBook);
  }

  /** Injects the FXML fields that {@code setRecipe} fills in. */
  private static void injectViews(
      RecipeDetailsViewPageController controller, Label name, VBox ingredients, VBox steps)
      throws Exception {
    setField(controller, "recipeNameLabel", name);
    setField(controller, "ingredientListBox", ingredients);
    setField(controller, "stepsListBox", steps);
  }

  private static void setField(Object target, String name, Object value) throws Exception {
    Field field = RecipeDetailsViewPageController.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }

  /** A recipe that needs two tomatoes. */
  private static Recipe saladRecipe(User user) {
    return new Recipe(
        "Salat", user, Map.of(new FoodItem("Tomat"), new Amount(2, Unit.PIECE)), List.of());
  }

  @Test
  void controllerCanBeCreated() {
    final RecipeDetailsViewPageController controller = new RecipeDetailsViewPageController();

    assertNotNull(controller);
  }

  @Test
  void setRecipeDisplaysRecipeName() throws Exception {
    runOnFxThread(
        () -> {
          final RecipeDetailsViewPageController controller = new RecipeDetailsViewPageController();
          Label nameLabel = new Label();
          injectViews(controller, nameLabel, new VBox(), new VBox());

          controller.setRecipe(new Recipe("Pasta", null));

          assertEquals("Pasta", nameLabel.getText());
        });
  }

  @Test
  void setRecipeHighlightsAvailableIngredients() throws Exception {
    runOnFxThread(
        () -> {
          final RecipeDetailsViewPageController controller = new RecipeDetailsViewPageController();

          final Recipe recipe =
              new Recipe(
                  "Tomatsalat",
                  null,
                  Map.of(
                      new FoodItem("Tomat"),
                      new Amount(2, Unit.PIECE),
                      new FoodItem("Agurk"),
                      new Amount(100, Unit.GRAM)),
                  List.of("Vask grønnsakene", "Kutt og bland"));
          Fridge fridge = new Fridge(null);
          fridge.addFoodItem(new FoodItem("Tomat"), new Amount(2, Unit.PIECE));
          App.setFridge(fridge);

          Label nameLabel = new Label();
          final VBox ingredientList = new VBox();
          final VBox stepsList = new VBox();
          injectViews(controller, nameLabel, ingredientList, stepsList);

          controller.setRecipe(recipe);

          assertEquals("Tomatsalat", nameLabel.getText());
          assertEquals(3, ingredientList.getChildren().size());
          assertEquals(2, stepsList.getChildren().size());

          Label availability = (Label) ingredientList.getChildren().get(0);
          CheckBox first = (CheckBox) ingredientList.getChildren().get(1);
          CheckBox second = (CheckBox) ingredientList.getChildren().get(2);
          CheckBox tomat = first.getText().contains("Tomat") ? first : second;
          CheckBox agurk = tomat == first ? second : first;

          assertEquals("Du har 1 av 2 ingredienser", availability.getText());
          assertEquals("-fx-background-color: #E8F5E9; -fx-cursor: hand;", tomat.getStyle());
          assertEquals("-fx-background-color: #FFFFFF; -fx-cursor: hand;", agurk.getStyle());

          assertEquals("1. Vask grønnsakene", ((Label) stepsList.getChildren().get(0)).getText());
          assertEquals("2. Kutt og bland", ((Label) stepsList.getChildren().get(1)).getText());
        });
  }

  /** Using a recipe removes its ingredients and sends the updated fridge to the server. */
  @Test
  void handleUseRecipeShouldUpdateFridgeOnServer() throws Exception {
    User user = new User("details-test");
    Fridge fridge = new Fridge(user);
    fridge.addFoodItem(new FoodItem("Tomat"), new Amount(3, Unit.PIECE));
    App.setFridge(fridge);
    App.setRecipeBook(new RecipeBook(user));
    Recipe recipe = saladRecipe(user);
    DinnertrackerRestClient restClient = mock(DinnertrackerRestClient.class);

    runOnFxThread(
        () -> {
          RecipeDetailsViewPageController controller =
              new RecipeDetailsViewPageController(restClient);
          VBox ingredientList = new VBox();
          injectViews(controller, new Label(), ingredientList, new VBox());
          controller.setRecipe(recipe);

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            controller.handleUseRecipe();
            alerts.verify(
                () ->
                    ShowAlertMessage.showInfoAlert(
                        "Oppskrift brukt", "Ingredienser fjernet fra kjøleskap.", ""));
          }

          verify(restClient).updateFridge(fridge);
          assertEquals(new Amount(1, Unit.PIECE), fridge.getFoodItems().get(new FoodItem("Tomat")));
          CheckBox checkBox = (CheckBox) ingredientList.getChildren().get(1);
          assertTrue(checkBox.isSelected());
        });
  }

  /** A failed request is reported to the user. */
  @Test
  void handleUseRecipeShouldReportServerFailure() throws Exception {
    User user = new User("details-test");
    Fridge fridge = new Fridge(user);
    fridge.addFoodItem(new FoodItem("Tomat"), new Amount(3, Unit.PIECE));
    App.setFridge(fridge);
    App.setRecipeBook(new RecipeBook(user));
    Recipe recipe = saladRecipe(user);
    DinnertrackerRestClient restClient = mock(DinnertrackerRestClient.class);
    doThrow(new IOException("nede")).when(restClient).updateFridge(fridge);

    runOnFxThread(
        () -> {
          RecipeDetailsViewPageController controller =
              new RecipeDetailsViewPageController(restClient);
          injectViews(controller, new Label(), new VBox(), new VBox());
          controller.setRecipe(recipe);

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            controller.handleUseRecipe();
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "Feil", "Kunne ikke lagre kjøleskapet.", "nede"));
          }
        });
  }

  /** Missing ingredients stop the action before anything is sent to the server. */
  @Test
  void handleUseRecipeShouldRejectMissingIngredients() throws Exception {
    User user = new User("details-test");
    App.setFridge(new Fridge(user));
    App.setRecipeBook(new RecipeBook(user));
    Recipe recipe = saladRecipe(user);
    DinnertrackerRestClient restClient = mock(DinnertrackerRestClient.class);

    runOnFxThread(
        () -> {
          RecipeDetailsViewPageController controller =
              new RecipeDetailsViewPageController(restClient);
          injectViews(controller, new Label(), new VBox(), new VBox());
          controller.setRecipe(recipe);

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            controller.handleUseRecipe();
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        eq("Kan ikke bruke oppskrift."), eq("Mangler ingredienser."), anyString()));
          }
          verifyNoInteractions(restClient);
        });
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}