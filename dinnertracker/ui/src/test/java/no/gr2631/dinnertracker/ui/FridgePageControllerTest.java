package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

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

        assertEquals(Unit.values().length,
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

        assertTrue(fridgeTable.getItems().stream()
                .anyMatch(row -> row.foodName().equals("Jordbær")));

        assertTrue(fridgeTable.getItems().stream()
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
    public void unitConverterShouldConvertUnitToSymbol() throws Exception {
        requireToolkit();

        StringConverter<Unit> converter =
                getUnitConverter();

        assertEquals("g", converter.toString(Unit.GRAM));
        assertEquals("kg", converter.toString(Unit.KILOGRAM));
        assertEquals("stk", converter.toString(Unit.PIECE));
    }

    /**
     * Tests that the unit converter converts symbols back to Units.
     */
    @Test
    public void unitConverterShouldConvertSymbolToUnit() throws Exception {
        requireToolkit();

        StringConverter<Unit> converter =
                getUnitConverter();

        assertEquals(Unit.GRAM, converter.fromString("g"));
        assertEquals(Unit.KILOGRAM, converter.fromString("kg"));
        assertEquals(Unit.PIECE, converter.fromString("stk"));
    }

    /**
     * Gets the StringConverter configured by initialize.
     *
     * @return the unit converter used by the ComboBox
     */
    @SuppressWarnings("unchecked")
    private StringConverter<Unit> getUnitConverter() {
        return unitComboBox.getConverter();
    }

    /**
     * Tests that a FridgeRow stores its food name and amount text.
     */
    @Test
    public void fridgeRowShouldStoreFoodNameAndAmount() {
        FridgePageController.FridgeRow row =
                new FridgePageController.FridgeRow("Melk", "2,0 L");

        assertEquals("Melk", row.foodName());
        assertEquals("2,0 L", row.amountText());
    }

    /**
    * Tests that an unknown unit symbol is converted to null.
    */
    @Test
    public void unitConverterShouldReturnNullForUnknownSymbol() {
        requireToolkit();

        StringConverter<Unit> converter = unitComboBox.getConverter();

        assertNull(converter.fromString("unknown"));
    }


    /**
    * Tests that converting a null unit returns an empty string.
    */
    @Test
    public void unitConverterShouldHandleNull() {
        requireToolkit();

        StringConverter<Unit> converter = unitComboBox.getConverter();

        assertEquals("", converter.toString(null));
    }

    /**
     * Tests that loadFridgeItems can load the fridge without a highlighted item.
     */
    @Test
    public void loadFridgeItemsShouldLoadWithoutHighlight() throws Exception {
        requireToolkit();

        Method method =
                FridgePageController.class.getDeclaredMethod(
                        "loadFridgeItems", String.class);

        method.setAccessible(true);
        method.invoke(controller, (String) null);

        assertFalse(fridgeTable.getItems().isEmpty());
        assertEquals(null, getHighlightedFoodName());
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
     * Tests that initialize configures the table columns.
     */
    @Test
    public void initializeShouldSetUpTableColumns() {
        requireToolkit();

        assertNotNull(foodNameColumn.getCellValueFactory());
        assertNotNull(amountColumn.getCellValueFactory());
    }

}
