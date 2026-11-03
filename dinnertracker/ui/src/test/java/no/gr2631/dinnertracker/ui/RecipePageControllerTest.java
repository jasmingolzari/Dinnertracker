package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.User;
import no.gr2631.dinnertracker.persistence.RecipeFileHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Simple tests for the methods in {@link RecipePageController}. The private fields and methods are
 * reached with reflection.
 *
 * <p>The name validation tests do not need JavaFX. The tests that use controls are skipped if the
 * JavaFX toolkit cannot start.
 */
public class RecipePageControllerTest {

  private static boolean toolkitStarted;

  private RecipePageController controller;
  private RecipeFileHandler recipeFileHandler;
  private TextField nameField;
  private TextField foodItemField;
  private Spinner<Double> amountSpinner;
  private ComboBox<Unit> unitComboBox;
  private ListView<String> ingredientsList;
  private TextField stepsField;
  private ListView<String> stepsList;
  private User previousUser;
  private RecipeBook previousRecipeBook;

  /** Tries to start the JavaFX toolkit so that controls can be created. */
  @BeforeAll
  public static void startToolkit() {
    try {
      Platform.startup(() -> {});
      Platform.setImplicitExit(false);
      toolkitStarted = true;
    } catch (IllegalStateException e) {
      Platform.setImplicitExit(false);
      toolkitStarted = true; // Already started.
    } catch (Throwable e) {
      toolkitStarted = false; // JavaFX could not start.
    }
  }

  /** Creates a controller, and new controls if JavaFX works, then calls {@code initialize()}. */
  @BeforeEach
  public void setUp() throws Exception {
    previousUser = App.getCurrentUser();
    previousRecipeBook = App.getRecipeBook();
    recipeFileHandler = mock(RecipeFileHandler.class);
    App.setCurrentUser(new User("testuser"));
    App.setRecipeBook(new RecipeBook(App.getCurrentUser()));
    controller = new RecipePageController(recipeFileHandler);
    if (!toolkitStarted) {
      return;
    }
    runOnFxThread(
        () -> {
          nameField = new TextField();
          foodItemField = new TextField();
          amountSpinner = new Spinner<>();
          unitComboBox = new ComboBox<>();
          ingredientsList = new ListView<>();
          stepsField = new TextField();
          stepsList = new ListView<>();

          setField("nameField", nameField);
          setField("foodItemField", foodItemField);
          setField("foodItemAmountSpinner", amountSpinner);
          setField("foodItemUnitComboBox", unitComboBox);
          setField("ingredientsList", ingredientsList);
          setField("stepsField", stepsField);
          setField("stepsList", stepsList);

          Method initialize = RecipePageController.class.getDeclaredMethod("initialize");
          initialize.setAccessible(true);
          initialize.invoke(controller);
        });
  }

  /** Restores the shared application state after each test. */
  @AfterEach
  public void tearDown() {
    App.setCurrentUser(previousUser);
    App.setRecipeBook(previousRecipeBook);
  }

  /** Skips the test if the JavaFX toolkit is not available. */
  private void requireToolkit() {
    assumeTrue(toolkitStarted, "JavaFX toolkit is not available");
  }

  /** Sets a private field in the controller. */
  private void setField(String name, Object value) throws Exception {
    Field field = RecipePageController.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(controller, value);
  }

  /** Calls a private controller method with one argument and returns the result. */
  private Object call(String name, Class<?> type, Object argument) throws Exception {
    Method method = RecipePageController.class.getDeclaredMethod(name, type);
    method.setAccessible(true);
    return method.invoke(controller, argument);
  }

  /** Checks whether the controller accepts an ingredient name. */
  private boolean isValid(String name) throws Exception {
    return (boolean) call("isValidFoodItemName", String.class, name);
  }

  /** Types an ingredient name and calls {@code addFoodItem}. */
  private void addIngredient(String name) throws Exception {
    foodItemField.setText(name);
    call("addFoodItem", ActionEvent.class, null);
  }

  /** Types a step and calls {@code addStep}. */
  private void addStep(String text) throws Exception {
    stepsField.setText(text);
    call("addStep", ActionEvent.class, null);
  }

  /** Runs a controller action on the JavaFX application thread. */
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

    assertTrue(completed.await(5, TimeUnit.SECONDS), "JavaFX action did not finish in time.");
    if (failure.get() != null) {
      throw new AssertionError("JavaFX action failed.", failure.get());
    }
  }

  /** Names with letters and hyphens are valid. */
  @Test
  public void validNamesShouldBeAccepted() throws Exception {
    assertTrue(isValid("Tomat"));
    assertTrue(isValid("Rød-løk"));
  }

  /** Empty names, digits, spaces and special characters are not valid. */
  @Test
  public void invalidNamesShouldBeRejected() throws Exception {
    assertFalse(isValid(null));
    assertFalse(isValid(""));
    assertFalse(isValid("   "));
    assertFalse(isValid("Tomat2"));
    assertFalse(isValid("Olive oil"));
    assertFalse(isValid("Salt!"));
  }

  /** initialize should configure the default amount, units and unit converter. */
  @Test
  public void initializeShouldConfigureAmountAndUnitControls() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          assertEquals(1.0, amountSpinner.getValue());
          assertEquals(Unit.GRAM, unitComboBox.getValue());
          assertEquals(Unit.values().length, unitComboBox.getItems().size());
          assertEquals(Unit.GRAM.getSymbol(), unitComboBox.getConverter().toString(Unit.GRAM));
          assertEquals("", unitComboBox.getConverter().toString(null));
          assertEquals(Unit.GRAM, unitComboBox.getConverter().fromString(Unit.GRAM.getSymbol()));
          assertNull(unitComboBox.getConverter().fromString("unknown"));
        });
  }

  /** addFoodItem should show the ingredient in the list and empty the fields. */
  @Test
  public void addFoodItemShouldShowIngredientAndClearFields() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          amountSpinner.getValueFactory().setValue(2.5);

          addIngredient("Tomat");

          assertEquals(List.of("Tomat : 2.5 " + Unit.GRAM.getSymbol()), ingredientsList.getItems());
          assertEquals("", foodItemField.getText());
          assertEquals(1.0, amountSpinner.getValue());
        });
  }

  /** Invalid ingredient names should be rejected. */
  @Test
  public void invalidFoodItemShouldShowAlert() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          foodItemField.setText("Tomat2");
          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            call("addFoodItem", ActionEvent.class, null);
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "Ugyldig input",
                        null,
                        "Ugyldig navn på matvare. Kun bokstaver og bindestrek er tillatt."));
          }
          assertTrue(ingredientsList.getItems().isEmpty());
        });
  }

  /** Ingredients without a selected unit should be rejected. */
  @Test
  public void missingFoodItemUnitShouldShowAlert() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          foodItemField.setText("Tomat");
          unitComboBox.setValue(null);
          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            call("addFoodItem", ActionEvent.class, null);
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "Ugyldig input", null, "Velg en enhet for mengden."));
          }
          assertTrue(ingredientsList.getItems().isEmpty());
        });
  }

  /** Ingredients should be listed in the order they were added. */
  @Test
  public void ingredientsShouldKeepOrder() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          addIngredient("Pasta");
          addIngredient("Saus");

          assertTrue(ingredientsList.getItems().get(0).startsWith("Pasta"));
          assertTrue(ingredientsList.getItems().get(1).startsWith("Saus"));
        });
  }

  /** addStep should show the step in the list and empty the field. */
  @Test
  public void addStepShouldShowStepAndClearField() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          addStep("Boil the pasta");

          assertEquals(List.of("Boil the pasta"), stepsList.getItems());
          assertEquals("", stepsField.getText());
        });
  }

  /** Steps should be listed in the order they were added. */
  @Test
  public void stepsShouldKeepOrder() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          addStep("First");
          addStep("Second");

          assertEquals(List.of("First", "Second"), stepsList.getItems());
        });
  }

  /** Empty steps should not be added. */
  @Test
  public void emptyStepShouldNotBeAdded() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          addStep("");
          addStep("   ");

          assertTrue(stepsList.getItems().isEmpty());
        });
  }

  /** clearForm should reset every field, list and stored ingredient. */
  @Test
  public void clearFormShouldResetAllInputs() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          nameField.setText("Pasta");
          foodItemField.setText("Tomat");
          stepsField.setText("Kok pasta");
          addIngredient("Tomat");
          addStep("Kok pasta");

          Method clearForm = RecipePageController.class.getDeclaredMethod("clearForm");
          clearForm.setAccessible(true);
          clearForm.invoke(controller);

          assertEquals("", nameField.getText());
          assertEquals("", foodItemField.getText());
          assertEquals("", stepsField.getText());
          assertTrue(ingredientsList.getItems().isEmpty());
          assertTrue(stepsList.getItems().isEmpty());
          assertTrue(controllerIngredients().isEmpty());
          assertEquals(1.0, amountSpinner.getValue());
          assertEquals(Unit.GRAM, unitComboBox.getValue());
        });
  }

  /** Recipes without names should be rejected. */
  @Test
  public void recipeWithoutNameShouldShowAlert() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            call("addRecipe", ActionEvent.class, null);
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "Ugyldig input", null, "Oppskriften må ha et navn."));
          }
          assertTrue(App.getRecipeBook().getRecipes().isEmpty());
          verifyNoInteractions(recipeFileHandler);
        });
  }

  /** Recipes without ingredients should be rejected. */
  @Test
  public void recipeWithoutIngredientsShouldShowAlert() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          nameField.setText("Pasta");
          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            call("addRecipe", ActionEvent.class, null);
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "Ugyldig input", null, "Legg til minst en ingrediens."));
          }
          assertTrue(App.getRecipeBook().getRecipes().isEmpty());
          verifyNoInteractions(recipeFileHandler);
        });
  }

  /** Recipes without steps should be rejected. */
  @Test
  public void recipeWithoutStepsShouldShowAlert() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          nameField.setText("Pasta");
          addIngredient("Tomat");
          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            call("addRecipe", ActionEvent.class, null);
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "Ugyldig input", null, "Legg til minst ett steg."));
          }
          assertTrue(App.getRecipeBook().getRecipes().isEmpty());
          verifyNoInteractions(recipeFileHandler);
        });
  }

  /** A complete recipe should be saved and the form should then be cleared. */
  @Test
  public void completeRecipeShouldBeSavedAndClearForm() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          nameField.setText("Testoppskrift");
          addIngredient("Tomat");
          addStep("Kutt tomat");

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            call("addRecipe", ActionEvent.class, null);
            alerts.verify(
                () -> ShowAlertMessage.showInfoAlert("Suksess", null, "Oppskrift lagt til"));
            verify(recipeFileHandler)
                .save(Path.of("..", "data", "recipes.json"), App.getRecipeBook());
            assertEquals(1, App.getRecipeBook().getRecipes().size());
            Recipe recipe = App.getRecipeBook().getRecipes().get(0);
            assertEquals("Testoppskrift", recipe.getName());
            assertSame(App.getCurrentUser(), recipe.getUserName());
            assertEquals(
                new Amount(1.0, Unit.GRAM), recipe.getIngredients().get(new FoodItem("Tomat")));
            assertEquals(List.of("Kutt tomat"), recipe.getSteps());
            alerts.verify(
                () -> ShowAlertMessage.showInfoAlert("Suksess", null, "Oppskrift lagt til"));
            assertEquals("", nameField.getText());
            assertTrue(ingredientsList.getItems().isEmpty());
            assertTrue(stepsList.getItems().isEmpty());
          }
        });
  }

  /** A failed save should report the error and keep the completed form available. */
  @Test
  public void failedSaveShouldShowAlertAndKeepForm() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          nameField.setText("Testoppskrift");
          addIngredient("Tomat");
          addStep("Kutt tomat");
          doThrow(new IOException("test failure"))
              .when(recipeFileHandler)
              .save(Path.of("..", "data", "recipes.json"), App.getRecipeBook());

          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            call("addRecipe", ActionEvent.class, null);
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "Klarte ikke å lagre oppskriften", null, "Feilen er: test failure"));
            alerts.verifyNoMoreInteractions();
          }
          assertEquals("Testoppskrift", nameField.getText());
          assertEquals(List.of("Tomat : 1.0 " + Unit.GRAM.getSymbol()), ingredientsList.getItems());
          assertEquals(List.of("Kutt tomat"), stepsList.getItems());
        });
  }

  /** Checks that the unit ComboBox cell displays the correct unit symbol and style. */
  @Test
  public void unitComboBoxShouldCreateCellWithCorrectUnitSymbol() throws Exception {
    requireToolkit();

    runOnFxThread(
        () -> {
          ListView<Unit> listView = new ListView<>();
          listView.getItems().add(Unit.GRAM);

          ListCell<Unit> cell = unitComboBox.getCellFactory().call(listView);

          cell.updateListView(listView);
          cell.updateIndex(0);

          assertEquals(Unit.GRAM, unitComboBox.getValue());
          assertEquals(Unit.values().length, unitComboBox.getItems().size());
          assertEquals(Unit.GRAM.getSymbol(), cell.getText());
          assertEquals("-fx-cursor: hand;", cell.getStyle());
        });
  }

  /** Returns the private ingredient map for checking that clearForm removes its state. */
  @SuppressWarnings("unchecked")
  private java.util.Map<Object, Object> controllerIngredients() throws Exception {
    Field field = RecipePageController.class.getDeclaredField("ingredients");
    field.setAccessible(true);
    return (java.util.Map<Object, Object>) field.get(controller);
  }

  /** backToHomePage should replace the current scene with the home page view. */
  @Test
  public void backToHomePageShouldNavigateHome() throws Exception {
    requireToolkit();
    runOnFxThread(
        () -> {
          Stage stage = new Stage();
          Button source = new Button();
          stage.setScene(new Scene(new VBox(source)));
          try {
            invokeBackToHomePage(source);
            assertEquals(BorderPane.class, stage.getScene().getRoot().getClass());
          } finally {
            stage.close();
          }
        });
  }

  /** Invokes the private back navigation method with a button as its event source. */
  private void invokeBackToHomePage(Button source) throws Exception {
    Method method =
        RecipePageController.class.getDeclaredMethod("backToHomePage", ActionEvent.class);
    method.setAccessible(true);
    method.invoke(controller, new ActionEvent(source, source));
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
