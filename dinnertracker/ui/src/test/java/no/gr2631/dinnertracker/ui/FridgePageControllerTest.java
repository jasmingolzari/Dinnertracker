package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/** Tests the fridge form, table contents, calls to the REST client, and navigation. */
public class FridgePageControllerTest {

  /** Whether the JavaFX toolkit could be started; the tests are skipped if it could not. */
  private static boolean toolkitStarted;

  /** The controller being tested. */
  private FridgePageController controller;

  /** Text field for the name of the food item to add. */
  private TextField foodItemField;

  /** Spinner for the quantity of the food item to add. */
  private Spinner<Double> quantitySpinner;

  /** Combo box for the unit of the food item to add. */
  private ComboBox<Unit> unitComboBox;

  /** Table that shows the contents of the fridge. */
  private TableView<Object> fridgeTable;

  /** Table column that shows the name of each food item. */
  private TableColumn<Object, String> foodNameColumn;

  /** Table column that shows the amount of each food item. */
  private TableColumn<Object, String> amountColumn;

  /** Mocked REST client, so no requests are sent to a real server. */
  private DinnertrackerRestClient restClient;

  /** The user who owns the test fridge. */
  private User testUser;

  /** Fridge used by the tests, with strawberries and apples in it. */
  private Fridge testFridge;

  // Original App state, restored after each test

  /** The logged-in user before the test, restored afterwards. */
  private User originalUser;

  /** The fridge in {@link App} before the test, restored afterwards. */
  private Fridge originalFridge;

  /** Starts JavaFX and keeps it running when a navigation test closes its stage. */
  @BeforeAll
  public static void startToolkit() {
    try {
      Platform.startup(() -> {});
      Platform.setImplicitExit(false);
      toolkitStarted = true;
    } catch (IllegalStateException exception) {
      Platform.setImplicitExit(false);
      toolkitStarted = true;
    } catch (Throwable exception) {
      toolkitStarted = false;
    }
  }

  /** Creates a populated fridge, injects a mock REST client, and initializes the controller. */
  @BeforeEach
  public void setUp() throws Exception {
    assumeTrue(toolkitStarted, "JavaFX toolkit is not available");

    originalUser = App.getCurrentUser();
    originalFridge = App.getFridge();

    testUser = new User("fridge-page-test");
    testFridge = new Fridge(testUser);
    testFridge.addFoodItem(new FoodItem("Jordbær"), new Amount(3.0, Unit.KILOGRAM));
    testFridge.addFoodItem(new FoodItem("Eple"), new Amount(5.0, Unit.PIECE));

    restClient = mock(DinnertrackerRestClient.class);

    App.setCurrentUser(testUser);
    App.setFridge(testFridge);

    runOnFxThread(
        () -> {
          controller = new FridgePageController(restClient);
          foodItemField = new TextField();
          quantitySpinner = new Spinner<>();
          unitComboBox = new ComboBox<>();
          fridgeTable = new TableView<>();
          foodNameColumn = new TableColumn<>();
          amountColumn = new TableColumn<>();
          fridgeTable.getColumns().add(foodNameColumn);
          fridgeTable.getColumns().add(amountColumn);

          setField("foodItemField", foodItemField);
          setField("quantitySpinner", quantitySpinner);
          setField("unitComboBox", unitComboBox);
          setField("fridgeTable", fridgeTable);
          setField("foodNameColumn", foodNameColumn);
          setField("amountColumn", amountColumn);

          Method initialize = FridgePageController.class.getDeclaredMethod("initialize");
          initialize.setAccessible(true);
          initialize.invoke(controller);
        });
  }

  /** Restores the shared App state after each test. */
  @AfterEach
  public void tearDown() {
    App.setCurrentUser(originalUser);
    App.setFridge(originalFridge);
  }

  /** Tests the default quantity. */
  @Test
  public void initializeShouldSetUpSpinner() throws Exception {
    runOnFxThread(
        () -> {
          assertNotNull(quantitySpinner.getValueFactory());
          assertEquals(0.0, quantitySpinner.getValue());
        });
  }

  /** Tests all available units and the default selection. */
  @Test
  public void initializeShouldSetUpUnitComboBox() throws Exception {
    runOnFxThread(
        () -> {
          assertEquals(Unit.values().length, unitComboBox.getItems().size());
          for (Unit unit : Unit.values()) {
            assertTrue(unitComboBox.getItems().contains(unit));
          }
          assertEquals(Unit.GRAM, unitComboBox.getValue());
        });
  }

  /** Tests the names and Norwegian amount formatting shown for the current fridge. */
  @Test
  public void initializeShouldDisplayFridgeItemsAndAmounts() throws Exception {
    runOnFxThread(
        () -> {
          assertEquals(2, fridgeTable.getItems().size());
          assertEquals(
              "3,0 kg", amountColumn.getCellObservableValue(rowNamed("Jordbær")).getValue());
          assertEquals("5,0 stk", amountColumn.getCellObservableValue(rowNamed("Eple")).getValue());
        });
  }

  /** Tests unit symbols, including an absent selection. */
  @Test
  public void unitConverterShouldConvertUnitToSymbol() throws Exception {
    runOnFxThread(
        () -> {
          assertEquals("g", unitComboBox.getConverter().toString(Unit.GRAM));
          assertEquals("kg", unitComboBox.getConverter().toString(Unit.KILOGRAM));
          assertEquals("stk", unitComboBox.getConverter().toString(Unit.PIECE));
          assertEquals("", unitComboBox.getConverter().toString(null));
        });
  }

  /** Tests symbol parsing and an unknown symbol. */
  @Test
  public void unitConverterShouldConvertSymbolToUnit() throws Exception {
    runOnFxThread(
        () -> {
          assertEquals(Unit.GRAM, unitComboBox.getConverter().fromString("g"));
          assertEquals(Unit.KILOGRAM, unitComboBox.getConverter().fromString("kg"));
          assertEquals(Unit.PIECE, unitComboBox.getConverter().fromString("stk"));
          assertNull(unitComboBox.getConverter().fromString("unknown"));
        });
  }

  /** Tests table bindings and alignment. */
  @Test
  public void initializeShouldSetUpTableColumns() throws Exception {
    runOnFxThread(
        () -> {
          assertNotNull(foodNameColumn.getCellValueFactory());
          assertNotNull(amountColumn.getCellValueFactory());
          assertEquals("-fx-alignment: CENTER_RIGHT;", amountColumn.getStyle());
        });
  }

  /** Tests a displayed food name without depending on the controller's private row type. */
  @Test
  public void foodNameColumnShouldReturnFoodName() throws Exception {
    runOnFxThread(
        () ->
            assertEquals(
                "Jordbær", foodNameColumn.getCellObservableValue(rowNamed("Jordbær")).getValue()));
  }

  /** Tests the displayed amount through the column's public interface. */
  @Test
  public void amountColumnShouldReturnAmountText() throws Exception {
    runOnFxThread(
        () ->
            assertEquals(
                "3,0 kg", amountColumn.getCellObservableValue(rowNamed("Jordbær")).getValue()));
  }

  /** Refreshing without a highlighted name keeps all items and clears the highlight. */
  @Test
  public void highlightFridgeItemShouldLoadWithoutHighlight() throws Exception {
    runOnFxThread(
        () -> {
          invoke("highlightFridgeItem", String.class, "Eple");
          invoke("highlightFridgeItem", String.class, null);
          assertEquals(2, fridgeTable.getItems().size());
          assertNull(highlightedFoodName());
        });
  }

  /** Refreshing stores the name of the row that should receive feedback. */
  @Test
  public void highlightFridgeItemShouldSetHighlightedFoodName() throws Exception {
    runOnFxThread(
        () -> {
          invoke("highlightFridgeItem", String.class, "Jordbær");
          assertEquals("Jordbær", highlightedFoodName());
        });
  }

  /** Refreshing from an empty fridge removes previous rows. */
  @Test
  public void highlightFridgeItemShouldClearRowsForEmptyFridge() throws Exception {
    runOnFxThread(
        () -> {
          App.setFridge(new Fridge(testUser));
          invoke("highlightFridgeItem", String.class, null);
          assertTrue(fridgeTable.getItems().isEmpty());
        });
  }

  /** Matching food names are highlighted without regard to case. */
  @Test
  public void rowFactoryShouldHighlightMatchingFood() throws Exception {
    runOnFxThread(
        () -> {
          setField("highlightedFoodName", "jordbær");
          TableRow<Object> row = fridgeTable.getRowFactory().call(fridgeTable);
          row.setItem(rowNamed("Jordbær"));
          assertEquals("-fx-background-color: rgba(47, 107, 59, 0.18);", row.getStyle());
        });
  }

  /** Other food names receive no highlight. */
  @Test
  public void rowFactoryShouldNotHighlightDifferentFood() throws Exception {
    runOnFxThread(
        () -> {
          setField("highlightedFoodName", "Jordbær");
          TableRow<Object> row = fridgeTable.getRowFactory().call(fridgeTable);
          row.setStyle("old style");
          row.setItem(rowNamed("Eple"));
          assertEquals("", row.getStyle());
        });
  }

  /** A valid addition sends the fridge to the server, refreshes its rows, and resets the inputs. */
  @Test
  public void addFoodItemShouldAddValidFoodItem() throws Exception {
    runOnFxThread(
        () -> {
          foodItemField.setText("  Testmat  ");
          quantitySpinner.getValueFactory().setValue(2.0);
          unitComboBox.setValue(Unit.KILOGRAM);
          invoke("addFoodItem", ActionEvent.class, null);
          assertEquals(
              new Amount(2.0, Unit.KILOGRAM),
              testFridge.getFoodItems().get(new FoodItem("Testmat")));
          verify(restClient).updateFridge(testFridge);
          assertEquals(3, fridgeTable.getItems().size());
          assertNotNull(rowNamed("Testmat"));
          assertEquals("Testmat", highlightedFoodName());
          assertEquals("", foodItemField.getText());
          assertEquals(0.0, quantitySpinner.getValue());
          assertEquals(Unit.GRAM, unitComboBox.getValue());
        });
  }

  /** Adding more of an existing ingredient updates its quantity instead of adding another row. */
  @Test
  public void addFoodItemShouldUpdateExistingFoodItem() throws Exception {
    runOnFxThread(
        () -> {
          foodItemField.setText("Eple");
          quantitySpinner.getValueFactory().setValue(2.0);
          unitComboBox.setValue(Unit.PIECE);
          invoke("addFoodItem", ActionEvent.class, null);
          assertEquals(
              new Amount(7.0, Unit.PIECE), testFridge.getFoodItems().get(new FoodItem("Eple")));
          assertEquals(2, fridgeTable.getItems().size());
          assertEquals("7,0 stk", amountColumn.getCellObservableValue(rowNamed("Eple")).getValue());
          verify(restClient).updateFridge(testFridge);
        });
  }

  /** An empty food name is rejected before anything is sent to the server. */
  @Test
  public void addFoodItemShouldRejectBlankName() throws Exception {
    runOnFxThread(
        () -> {
          foodItemField.setText("   ");
          quantitySpinner.getValueFactory().setValue(1.0);
          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            invoke("addFoodItem", ActionEvent.class, null);
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "Ugyldig vare", null, "Du må oppgi et navn for matvaren."));
          }
          assertEquals(2, testFridge.getFoodItems().size());
          verifyNoInteractions(restClient);
        });
  }

  /** A zero amount is rejected before anything is sent to the server. */
  @Test
  public void addFoodItemShouldRejectZeroQuantity() throws Exception {
    runOnFxThread(
        () -> {
          foodItemField.setText("Testmat");
          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            invoke("addFoodItem", ActionEvent.class, null);
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "Ugyldig vare", null, "Mengden må være endelig og større enn null."));
          }
          assertEquals(2, testFridge.getFoodItems().size());
          verifyNoInteractions(restClient);
        });
  }

  /** A missing unit is rejected before anything is sent to the server. */
  @Test
  public void addFoodItemShouldRejectMissingUnit() throws Exception {
    runOnFxThread(
        () -> {
          foodItemField.setText("Testmat");
          quantitySpinner.getValueFactory().setValue(1.0);
          unitComboBox.setValue(null);
          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            invoke("addFoodItem", ActionEvent.class, null);
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "Ugyldig vare", null, "Enhet kan ikke være null."));
          }
          assertEquals(2, testFridge.getFoodItems().size());
          verifyNoInteractions(restClient);
        });
  }

  /** A failed request reports the error and leaves the form available. */
  @Test
  public void failedSaveShouldShowAlertAndKeepInputs() throws Exception {
    runOnFxThread(
        () -> {
          foodItemField.setText("Testmat");
          quantitySpinner.getValueFactory().setValue(2.0);
          unitComboBox.setValue(Unit.KILOGRAM);
          doThrow(new IOException("test failure")).when(restClient).updateFridge(testFridge);
          try (MockedStatic<ShowAlertMessage> alerts = mockStatic(ShowAlertMessage.class)) {
            invoke("addFoodItem", ActionEvent.class, null);
            alerts.verify(
                () ->
                    ShowAlertMessage.showErrorAlert(
                        "En uforventet feil oppsto", null, "Feilen er: test failure"));
          }
          assertEquals("Testmat", foodItemField.getText());
          assertEquals(2.0, quantitySpinner.getValue());
          assertEquals(Unit.KILOGRAM, unitComboBox.getValue());
        });
  }

  /** The back action replaces the current scene with the home page. */
  @Test
  public void backToHomePageShouldNavigateHome() throws Exception {
    runOnFxThread(
        () -> {
          Stage stage = new Stage();
          try {
            Button source = new Button();
            stage.setScene(new Scene(new VBox(source)));
            invoke("backToHomePage", ActionEvent.class, new ActionEvent(source, source));
            assertEquals(BorderPane.class, stage.getScene().getRoot().getClass());
          } finally {
            stage.close();
          }
        });
  }

  /**
   * Sets a private field in the controller.
   *
   * @param name the name of the field
   * @param value the value to set
   * @throws Exception if the field does not exist or cannot be set
   */
  private void setField(String name, Object value) throws Exception {
    Field field = FridgePageController.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(controller, value);
  }

  /**
   * Calls a private controller method that takes one argument.
   *
   * @param name the name of the method
   * @param argumentType the type of the method's parameter
   * @param argument the value to pass to the method
   * @throws Exception if the method does not exist or throws an exception
   */
  private void invoke(String name, Class<?> argumentType, Object argument) throws Exception {
    Method method = FridgePageController.class.getDeclaredMethod(name, argumentType);
    method.setAccessible(true);
    method.invoke(controller, argument);
  }

  /**
   * Reads the name of the food item the controller currently highlights.
   *
   * @return the highlighted food name, or {@code null} if no row is highlighted
   * @throws Exception if the field does not exist or cannot be read
   */
  private String highlightedFoodName() throws Exception {
    Field field = FridgePageController.class.getDeclaredField("highlightedFoodName");
    field.setAccessible(true);
    return (String) field.get(controller);
  }

  /**
   * Finds the table row for a food item, using the name column so the test does not depend on the
   * controller's private row type.
   *
   * @param name the food name to look for
   * @return the matching row
   * @throws java.util.NoSuchElementException if no row has that name
   */
  private Object rowNamed(String name) {
    return fridgeTable.getItems().stream()
        .filter(row -> foodNameColumn.getCellObservableValue(row).getValue().equals(name))
        .findFirst()
        .orElseThrow();
  }

  /**
   * Runs JavaFX work on its application thread and propagates assertion failures.
   *
   * @param action the work to run
   * @throws Exception if the work does not finish within five seconds or fails
   */
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

  /** A piece of work that, unlike {@link Runnable}, may throw a checked exception. */
  @FunctionalInterface
  private interface ThrowingRunnable {
    /**
     * Runs the work.
     *
     * @throws Exception if the work fails
     */
    void run() throws Exception;
  }
}
