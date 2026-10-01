package no.ntnu.it1901.gr2631.dinnertracker.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import no.ntnu.it1901.gr2631.dinnertracker.core.Amount;
import no.ntnu.it1901.gr2631.dinnertracker.core.FoodItem;
import no.ntnu.it1901.gr2631.dinnertracker.core.Recipe;
import no.ntnu.it1901.gr2631.dinnertracker.core.Unit;
import no.ntnu.it1901.gr2631.dinnertracker.persistence.RecipeFileHandler;

/**
 * Controller for the recipe creation page.
 * Handles user input for recipe names, ingredients, quantities,
 * units and preparation steps.
 */
public class RecipePageController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField foodItemField;

    @FXML
    private Spinner<Double> foodItemAmountSpinner;

    @FXML
    private ComboBox<Unit> foodItemUnitComboBox;

    @FXML
    private Button addFoodItemButton;

    @FXML
    private ListView<String> ingredientsList;

    @FXML
    private TextField stepsField;

    @FXML
    private Button addStepButton;

    @FXML
    private ListView<String> stepsList;

    @FXML
    private Button backButton;

    @FXML
    private Button addRecipeButton;

    /**
     * Stores the ingredients added to the recipe together with
     * their corresponding quantities and units.
     */
    private final Map<FoodItem, Amount> ingredients = new LinkedHashMap<>();

    /**
     * Initializes the recipe page.
     * Sets up the quantity spinner and the unit selection dropdown.
     * Gram is selected as the default unit.
     */
    @FXML
    private void initialize() {
        foodItemAmountSpinner.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        0.1, 1000.0, 1.0, 0.1
                )
        );

        foodItemUnitComboBox.getItems().setAll(Unit.values());

        foodItemUnitComboBox.setConverter(new StringConverter<Unit>() {

            /**
             * Converts a unit to its symbol for display in the ComboBox.
             *
             * @param unit the unit to convert
             * @return the symbol of the unit
             */
            @Override
            public String toString(Unit unit) {
                if (unit == null) {
                    return "";
                }

                return unit.getSymbol();
            }

            /**
             * Converts a displayed unit symbol back to a Unit.
             *
             * @param string the unit symbol
             * @return the corresponding Unit, or null if no unit matches
             */
            @Override
            public Unit fromString(String string) {
                for (Unit unit : Unit.values()) {
                    if (unit.getSymbol().equals(string)) {
                        return unit;
                    }
                }

                return null;
            }
        });

        foodItemUnitComboBox.setValue(Unit.GRAM);
    }

    /**
     * Adds an ingredient to the current recipe.
     * Creates a FoodItem and an Amount using the selected quantity
     * and unit, then displays the ingredient in the ingredient list.
     *
     * @param event the action event triggered by the add button
     */
    @FXML
    private void addFoodItem(ActionEvent event) {
        String foodName = foodItemField.getText();

        if (!isValidFoodItemName(foodName)) {
            showAlertMessage(
                    "Ugyldig navn på matvare. Kun bokstaver og bindestrek er tillatt."
            );
            return;
        }

        double quantity = foodItemAmountSpinner.getValue();
        Unit unit = foodItemUnitComboBox.getValue();

        if (unit == null) {
            showAlertMessage("Velg en enhet for mengden.");
            return;
        }

        FoodItem newFoodItem = new FoodItem(foodName);
        Amount amount = new Amount(quantity, unit);

        ingredients.put(newFoodItem, amount);

        ingredientsList.getItems().add(
                foodName + " - " + quantity + " " + unit.getSymbol()
        );

        foodItemField.clear();
        foodItemAmountSpinner.getValueFactory().setValue(1.0);
        foodItemUnitComboBox.setValue(Unit.GRAM);
    }

    /**
     * Checks whether a food item name is valid.
     * The name must not be blank and may only contain letters and hyphens.
     *
     * @param foodName the food item name to validate
     * @return true if the name is valid, otherwise false
     */
    private boolean isValidFoodItemName(String foodName) {
        if (foodName == null || foodName.isBlank()) {
            return false;
        }

        for (char c : foodName.toCharArray()) {
            if (!Character.isLetter(c) && c != '-') {
                return false;
            }
        }

        return true;
    }

    /**
     * Displays an error alert with the given message.
     *
     * @param message the message to display
     */
    private void showAlertMessage(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Ugyldig input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Adds a preparation step to the current recipe.
     * Empty or blank steps are ignored.
     *
     * @param event the action event triggered by the add button
     */
    @FXML
    private void addStep(ActionEvent event) {
        String step = stepsField.getText();

        if (step != null && !step.isBlank()) {
            stepsList.getItems().add(step);
            stepsField.clear();
        }
    }

    /**
     * Creates and saves the recipe.
     * Validates the recipe name, ingredients and preparation steps
     * before writing the recipe to the JSON file.
     *
     * @param event the action event triggered by the add recipe button
     */
    @FXML
    private void addRecipe(ActionEvent event) {
        String recipeName = nameField.getText();

        if (recipeName == null || recipeName.isBlank()) {
            showAlertMessage("Oppskriften må ha et navn.");
            return;
        }

        if (ingredients.isEmpty()) {
            showAlertMessage("Legg til minst en ingrediens.");
            return;
        }

        List<String> steps = new ArrayList<>(stepsList.getItems());

        if (steps.isEmpty()) {
            showAlertMessage("Legg til minst ett steg.");
            return;
        }

        Recipe newRecipe = new Recipe(
                recipeName,
                new LinkedHashMap<>(ingredients),
                steps
        );

        Path filePath = Path.of("..", "data", "recipes.json");
        RecipeFileHandler fileHandler = new RecipeFileHandler();

        try {
            fileHandler.addRecipe(newRecipe, filePath);

            showAddedRecipeMessage("Oppskrift lagt til");
            clearForm();

        } catch (IOException e) {
            showAlertMessage(
                    "Klarte ikke å lagre oppskriften: " + e.getMessage()
            );
        }
    }

    /**
     * Displays an information alert after a recipe has been successfully added.
     *
     * @param message the message to display
     */
    private void showAddedRecipeMessage(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Suksess");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Clears all input fields and removes all ingredients and steps
     * from the current recipe.
     */
    private void clearForm() {
        nameField.clear();
        foodItemField.clear();
        stepsField.clear();

        ingredientsList.getItems().clear();
        stepsList.getItems().clear();

        ingredients.clear();

        foodItemAmountSpinner.getValueFactory().setValue(1.0);
        foodItemUnitComboBox.setValue(Unit.GRAM);
    }

    /**
     * Navigates back to the home page.
     *
     * @param event the action event triggered by the back button
     * @throws IOException if the home page FXML file cannot be loaded
     */
    @FXML
    private void backToHomePage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("HomePage.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }
}