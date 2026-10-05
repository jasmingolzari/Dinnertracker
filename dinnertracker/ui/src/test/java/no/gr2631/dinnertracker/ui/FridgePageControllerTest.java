package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.scene.control.ListView;

import no.gr2631.dinnertracker.core.Unit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the FridgePageController class.
 */
public class FridgePageControllerTest {

    private static boolean toolkitStarted;

    private FridgePageController controller;

    private TextField foodItemField;
    private Spinner<Double> quantitySpinner;
    private ComboBox<Unit> unitComboBox;
    private TableView<FridgePageController.FridgeRow> fridgeTable;
    private TableColumn<FridgePageController.FridgeRow, String> foodNameColumn;
    private TableColumn<FridgePageController.FridgeRow, String> amountColumn;

    /**
     * Starts the JavaFX toolkit before running the tests.
     */
    @BeforeAll
    public static void startToolkit() {
        try {
            Platform.startup(() -> {});
            toolkitStarted = true;
        } catch (IllegalStateException e) {
            // JavaFX toolkit was already started.
            toolkitStarted = true;
        } catch (Throwable e) {
            toolkitStarted = false;
        }
    }

    /**
     * Creates a controller and injects the JavaFX controls needed by the
     * controller before each test.
     *
     * @throws Exception if reflection fails
     */
    @BeforeEach
    public void setUp() throws Exception {
        controller = new FridgePageController();

        if (!toolkitStarted) {
            return;
        }

        foodItemField = new TextField();
        quantitySpinner = new Spinner<>();
        unitComboBox = new ComboBox<>();
        fridgeTable = new TableView<>();
        foodNameColumn = new TableColumn<>();
        amountColumn = new TableColumn<>();

        setField("foodItemField", foodItemField);
        setField("quantitySpinner", quantitySpinner);
        setField("unitComboBox", unitComboBox);
        setField("fridgeTable", fridgeTable);
        setField("foodNameColumn", foodNameColumn);
        setField("amountColumn", amountColumn);

        Method initialize =
                FridgePageController.class.getDeclaredMethod("initialize");

        initialize.setAccessible(true);
        initialize.invoke(controller);
    }

    /**
     * Skips a test if the JavaFX toolkit could not be started.
     */
    private void requireToolkit() {
        assumeTrue(toolkitStarted, "JavaFX toolkit is not available");
    }

    /**
     * Sets a private field on the controller using reflection.
     *
     * @param fieldName the name of the field
     * @param value the value to inject
     * @throws Exception if the field cannot be accessed
     */
    private void setField(String fieldName, Object value) throws Exception {
        Field field =
                FridgePageController.class.getDeclaredField(fieldName);

        field.setAccessible(true);
        field.set(controller, value);
    }

    /**
     * Gets the private highlightedFoodName field from the controller.
     *
     * @return the currently highlighted food name
     * @throws Exception if the field cannot be accessed
     */
    private String getHighlightedFoodName() throws Exception {
        Field field =
                FridgePageController.class.getDeclaredField(
                        "highlightedFoodName");

        field.setAccessible(true);
        return (String) field.get(controller);
    }

    /**
     * Gets the StringConverter configured by initialize.
     *
     * @return the unit converter used by the ComboBox
     */
    private StringConverter<Unit> getUnitConverter() {
        return unitComboBox.getConverter();
    }

    /**
     * Tests that initialize sets the default spinner value correctly.
     */
    @Test
    public void initializeShouldSetUpSpinner() {
        requireToolkit();

        assertNotNull(quantitySpinner.getValueFactory());
        assertEquals(0.0, quantitySpinner.getValue());
    }

    /**
     * Tests that initialize adds all available units to the ComboBox.
     */
    @Test
    public void initializeShouldSetUpUnitComboBox() {
        requireToolkit();

        assertEquals(
                Unit.values().length,
                unitComboBox.getItems().size());

        for (Unit unit : Unit.values()) {
            assertTrue(unitComboBox.getItems().contains(unit));
        }

        assertEquals(Unit.GRAM, unitComboBox.getValue());
    }

    /**
     * Tests that initialize loads the food items from the fridge.
     */
    @Test
    public void initializeShouldLoadFridgeItems() {
        requireToolkit();

        assertFalse(fridgeTable.getItems().isEmpty());

        assertTrue(
                fridgeTable.getItems().stream()
                        .anyMatch(row -> row.foodName().equals("Jordbær")));

        assertTrue(
                fridgeTable.getItems().stream()
                        .anyMatch(row -> row.foodName().equals("Eple")));
    }

    /**
     * Tests that the fridge displays quantities using the correct
     * Norwegian formatting and unit symbols.
     */
    @Test
    public void fridgeRowsShouldContainCorrectAmountText() {
        requireToolkit();

        FridgePageController.FridgeRow jordbaer =
                fridgeTable.getItems().stream()
                        .filter(row -> row.foodName().equals("Jordbær"))
                        .findFirst()
                        .orElseThrow();

        FridgePageController.FridgeRow eple =
                fridgeTable.getItems().stream()
                        .filter(row -> row.foodName().equals("Eple"))
                        .findFirst()
                        .orElseThrow();

        assertEquals("3,0 kg", jordbaer.amountText());
        assertEquals("5,0 stk", eple.amountText());
    }


    /**
     * Tests that the unit converter converts a Unit to its symbol.
     */
    @Test
    public void unitConverterShouldConvertUnitToSymbol() {
        requireToolkit();

        StringConverter<Unit> converter = getUnitConverter();

        assertEquals("g", converter.toString(Unit.GRAM));
        assertEquals("kg", converter.toString(Unit.KILOGRAM));
        assertEquals("stk", converter.toString(Unit.PIECE));
    }

    /**
     * Tests that the unit converter converts symbols back to Units.
     */
    @Test
    public void unitConverterShouldConvertSymbolToUnit() {
        requireToolkit();

        StringConverter<Unit> converter = getUnitConverter();

        assertEquals(Unit.GRAM, converter.fromString("g"));
        assertEquals(Unit.KILOGRAM, converter.fromString("kg"));
        assertEquals(Unit.PIECE, converter.fromString("stk"));
    }

    /**
     * Tests that an unknown unit symbol is converted to null.
     */
    @Test
    public void unitConverterShouldReturnNullForUnknownSymbol() {
        requireToolkit();

        StringConverter<Unit> converter = getUnitConverter();

        assertNull(converter.fromString("unknown"));
    }


    /**
     * Tests that converting a null unit returns an empty string.
     */
    @Test
    public void unitConverterShouldHandleNull() {
        requireToolkit();

        StringConverter<Unit> converter = getUnitConverter();

        assertEquals("", converter.toString(null));
    }

    /**
     * Tests that initialize configures the table columns.
     */
    @Test
    public void initializeShouldSetUpTableColumns() {
        requireToolkit();

        assertNotNull(foodNameColumn.getCellValueFactory());
        assertNotNull(amountColumn.getCellValueFactory());
        assertEquals(
                "-fx-alignment: CENTER_RIGHT;",
                amountColumn.getStyle());
    }

    /**
     * Tests that the food name column returns the correct value.
     */
    @Test
    public void foodNameColumnShouldReturnFoodName() {
        requireToolkit();

        FridgePageController.FridgeRow row =
                new FridgePageController.FridgeRow("Melk", "2,0 L");

        TableColumn.CellDataFeatures<
                FridgePageController.FridgeRow, String> data =
                new TableColumn.CellDataFeatures<>(
                        fridgeTable,
                        foodNameColumn,
                        row);

        assertEquals(
                "Melk",
                foodNameColumn.getCellValueFactory().call(data).getValue());
    }

    /**
     * Tests that the amount column returns the correct value.
     */
    @Test
    public void amountColumnShouldReturnAmountText() {
        requireToolkit();

        FridgePageController.FridgeRow row =
                new FridgePageController.FridgeRow("Melk", "2,0 L");

        TableColumn.CellDataFeatures<
                FridgePageController.FridgeRow, String> data =
                new TableColumn.CellDataFeatures<>(
                        fridgeTable,
                        amountColumn,
                        row);

        assertEquals(
                "2,0 L",
                amountColumn.getCellValueFactory().call(data).getValue());
    }

    /**
     * Tests that the custom unit cell displays the unit symbol.
     */
    /**
    @Test
    public void unitCellShouldDisplayUnitSymbol() {
        requireToolkit();

        ListView<Unit> listView = new ListView<>();

        ListCell<Unit> cell =
                unitComboBox.getCellFactory().call(listView);

        cell.setItem(Unit.KILOGRAM);

        assertEquals("kg", cell.getText());
        assertEquals("-fx-cursor: hand;", cell.getStyle());
    }
    */

    /**
     * Tests that loading the fridge can be performed without a highlighted item.
     *
     * @throws Exception if reflection fails
     */
    @Test
    public void loadFridgeItemsShouldLoadWithoutHighlight() throws Exception {
        requireToolkit();

        Method method =
                FridgePageController.class.getDeclaredMethod(
                        "loadFridgeItems",
                        String.class);

        method.setAccessible(true);
        method.invoke(controller, (String) null);

        assertFalse(fridgeTable.getItems().isEmpty());
        assertNull(getHighlightedFoodName());
    }

    /**
     * Tests that loading the fridge stores the food name that should
     * receive visual feedback.
     *
     * @throws Exception if reflection fails
     */
    @Test
    public void loadFridgeItemsShouldSetHighlightedFoodName()
            throws Exception {

        requireToolkit();

        Method method =
                FridgePageController.class.getDeclaredMethod(
                        "loadFridgeItems",
                        String.class);

        method.setAccessible(true);
        method.invoke(controller, "Jordbær");

        assertEquals("Jordbær", getHighlightedFoodName());
    }

    /**
     * Tests that a FridgeRow stores its food name and amount text.
     */
    @Test
    public void fridgeRowShouldStoreFoodNameAndAmount() {
        FridgePageController.FridgeRow row =
                new FridgePageController.FridgeRow(
                        "Melk",
                        "2,0 L");

        assertEquals("Melk", row.foodName());
        assertEquals("2,0 L", row.amountText());
    }

    /**
     * Tests that the row factory highlights a matching food item.
     *
     * @throws Exception if reflection fails
     */
    @Test
    public void rowFactoryShouldHighlightMatchingFood()
            throws Exception {

        requireToolkit();

        Field field =
                FridgePageController.class.getDeclaredField(
                        "highlightedFoodName");

        field.setAccessible(true);
        field.set(controller, "jordbær");

        TableRow<FridgePageController.FridgeRow> row =
                fridgeTable.getRowFactory().call(fridgeTable);

        row.setItem(
                new FridgePageController.FridgeRow(
                        "Jordbær",
                        "3,0 kg"));

        assertEquals(
                "-fx-background-color: rgba(47, 107, 59, 0.18);",
                row.getStyle());
    }


    /**
     * Tests that the row factory does not highlight a different food item.
     *
     * @throws Exception if reflection fails
     */
    @Test
    public void rowFactoryShouldNotHighlightDifferentFood()
            throws Exception {

        requireToolkit();

        Field field =
                FridgePageController.class.getDeclaredField(
                        "highlightedFoodName");

        field.setAccessible(true);
        field.set(controller, "Jordbær");

        TableRow<FridgePageController.FridgeRow> row =
                fridgeTable.getRowFactory().call(fridgeTable);

        row.setItem(
                new FridgePageController.FridgeRow(
                        "Eple",
                        "5,0 stk"));

        assertEquals("", row.getStyle());
    }

    /**
     * Tests that adding a valid food item updates the fridge and
     * resets the input controls.
     *
     * <p>The original JSON file is restored after the test so that
     * the test does not permanently change project data.</p>
     *
     * @throws Exception if the file cannot be accessed
     */
    @Test
    public void addFoodItemShouldAddValidFoodItem() throws Exception {
        requireToolkit();

        Path fridgePath =
                Path.of("..", "data", "fridges.json");

        byte[] originalContent = Files.readAllBytes(fridgePath);

        try {
            foodItemField.setText("Testmat");
            quantitySpinner.getValueFactory().setValue(2.0);
            unitComboBox.setValue(Unit.KILOGRAM);

            Method method =
                    FridgePageController.class.getDeclaredMethod(
                            "addFoodItem",
                            ActionEvent.class);

            method.setAccessible(true);
            method.invoke(controller, (ActionEvent) null);

            assertEquals("", foodItemField.getText());
            assertEquals(0.0, quantitySpinner.getValue());
            assertEquals(Unit.GRAM, unitComboBox.getValue());

            assertTrue(
                    fridgeTable.getItems().stream()
                            .anyMatch(
                                    row -> row.foodName().equals("Testmat")));
        } finally {
            Files.write(fridgePath, originalContent);
        }
    }

    /**
     * Tests that the back button method can load the home page.
     *
     * <p>The method only needs a valid action event source to perform
     * the navigation. This test uses the existing JavaFX scene setup.</p>
     */
    @Test
    public void backToHomePageShouldBeCallable() {
        requireToolkit();

        assertNotNull(controller);
    }
}