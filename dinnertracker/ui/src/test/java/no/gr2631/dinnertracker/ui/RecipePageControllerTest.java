package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import no.gr2631.dinnertracker.core.Unit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Simple tests for the methods in {@link RecipePageController}. The private fields and methods are
 * reached with reflection. Only methods that do not open an alert are called.
 *
 * <p>The name validation tests do not need JavaFX. The tests that use controls are skipped if the
 * JavaFX toolkit cannot start.
 */
public class RecipePageControllerTest {

  private static boolean toolkitStarted;

  private RecipePageController controller;
  private TextField foodItemField;
  private Spinner<Double> amountSpinner;
  private ComboBox<Unit> unitComboBox;
  private ListView<String> ingredientsList;
  private TextField stepsField;
  private ListView<String> stepsList;

  /** Tries to start the JavaFX toolkit so that controls can be created. */
  @BeforeAll
  public static void startToolkit() {
    try {
      Platform.startup(() -> {});
      toolkitStarted = true;
    } catch (IllegalStateException e) {
      toolkitStarted = true; // Already started.
    } catch (Throwable e) {
      toolkitStarted = false; // JavaFX could not start.
    }
  }

  /** Creates a controller, and new controls if JavaFX works, then calls {@code initialize()}. */
  @BeforeEach
  public void setUp() throws Exception {
    controller = new RecipePageController();
    if (!toolkitStarted) {
      return;
    }
    foodItemField = new TextField();
    amountSpinner = new Spinner<>();
    unitComboBox = new ComboBox<>();
    ingredientsList = new ListView<>();
    stepsField = new TextField();
    stepsList = new ListView<>();

    setField("foodItemField", foodItemField);
    setField("foodItemAmountSpinner", amountSpinner);
    setField("foodItemUnitComboBox", unitComboBox);
    setField("ingredientsList", ingredientsList);
    setField("stepsField", stepsField);
    setField("stepsList", stepsList);

    Method initialize = RecipePageController.class.getDeclaredMethod("initialize");
    initialize.setAccessible(true);
    initialize.invoke(controller);
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

  /** addFoodItem should show the ingredient in the list and empty the fields. */
  @Test
  public void addFoodItemShouldShowIngredientAndClearFields() throws Exception {
    requireToolkit();
    amountSpinner.getValueFactory().setValue(2.5);

    addIngredient("Tomat");

    assertEquals(List.of("Tomat - 2.5 " + Unit.GRAM.getSymbol()), ingredientsList.getItems());
    assertEquals("", foodItemField.getText());
    assertEquals(1.0, amountSpinner.getValue());
  }

  /** Ingredients should be listed in the order they were added. */
  @Test
  public void ingredientsShouldKeepOrder() throws Exception {
    requireToolkit();
    addIngredient("Pasta");
    addIngredient("Saus");

    assertTrue(ingredientsList.getItems().get(0).startsWith("Pasta"));
    assertTrue(ingredientsList.getItems().get(1).startsWith("Saus"));
  }

  /** addStep should show the step in the list and empty the field. */
  @Test
  public void addStepShouldShowStepAndClearField() throws Exception {
    requireToolkit();
    addStep("Boil the pasta");

    assertEquals(List.of("Boil the pasta"), stepsList.getItems());
    assertEquals("", stepsField.getText());
  }

  /** Steps should be listed in the order they were added. */
  @Test
  public void stepsShouldKeepOrder() throws Exception {
    requireToolkit();
    addStep("First");
    addStep("Second");

    assertEquals(List.of("First", "Second"), stepsList.getItems());
  }

  /** Empty steps should not be added. */
  @Test
  public void emptyStepShouldNotBeAdded() throws Exception {
    requireToolkit();
    addStep("");
    addStep("   ");

    assertTrue(stepsList.getItems().isEmpty());
  }
}
