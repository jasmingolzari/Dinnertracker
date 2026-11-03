package no.gr2631.dinnertracker.ui;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.persistence.RecipeFileHandler;

/**
 * Controller for the recipe creation page. Handles user input for recipe names, ingredients,
 * quantities, units and preparation steps.
 */
public class RecipePageController {

  private final RecipeFileHandler recipeFileHandler;

  @FXML private TextField nameField;

  @FXML private TextField foodItemField;

  @FXML private Spinner<Double> foodItemAmountSpinner;

  @FXML private ComboBox<Unit> foodItemUnitComboBox;

  @FXML private ListView<String> ingredientsList;

  @FXML private TextField stepsField;

  @FXML private ListView<String> stepsList;

  /**
   * Stores the ingredients added to the recipe together with their corresponding quantities and
   * units.
   */
  private final Map<FoodItem, Amount> ingredients = new LinkedHashMap<>();

  /** Creates the FXML controller with the application's shared recipe file handler. */
  public RecipePageController() {
    this(App.recipeFileHandler);
  }

  /**
   * Creates a controller with the supplied recipe file handler.
   *
   * @param recipeFileHandler the handler used to save recipes
   */
  RecipePageController(RecipeFileHandler recipeFileHandler) {
    this.recipeFileHandler = recipeFileHandler;
  }

  /**
   * Initializes the recipe page. Sets up the quantity spinner and the unit selection dropdown. Gram
   * is selected as the default unit.
   */
  @FXML
  public void initialize() {
    foodItemAmountSpinner.setValueFactory(
        new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1000.0, 1.0, 0.1));

    foodItemUnitComboBox.getItems().setAll(Unit.values());
    foodItemUnitComboBox.setValue(Unit.GRAM);
    foodItemUnitComboBox.setCellFactory(
        comboBox ->
            new ListCell<>() {
              @Override
              protected void updateItem(Unit unit, boolean empty) {
                super.updateItem(unit, empty);
                setText(empty || unit == null ? null : unit.getSymbol());
                setStyle(empty ? "" : "-fx-cursor: hand;");
              }
            });

    stepsList.setCellFactory(
        listView ->
            new ListCell<>() {
              @Override
              protected void updateItem(String step, boolean empty) {
                super.updateItem(step, empty);
                setText(empty || step == null ? null : (getIndex() + 1) + ". " + step);
              }
            });

    foodItemUnitComboBox.setConverter(
        new StringConverter<Unit>() {

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
  }

  /**
   * Adds an ingredient to the current recipe. Creates a FoodItem and an Amount using the selected
   * quantity and unit, then displays the ingredient in the ingredient list.
   *
   * @param event the action event triggered by the add button
   */
  @FXML
  public void addFoodItem(ActionEvent event) {
    try {
      String foodName = foodItemField.getText();

      if (!isValidFoodItemName(foodName)) {
        ShowAlertMessage.showErrorAlert(
            "Ugyldig input",
            null,
            "Ugyldig navn på matvare. Kun bokstaver og bindestrek er tillatt.");
        return;
      }

      double quantity = foodItemAmountSpinner.getValue();
      Unit unit = foodItemUnitComboBox.getValue();

      if (unit == null) {
        ShowAlertMessage.showErrorAlert("Ugyldig input", null, "Velg en enhet for mengden.");
        return;
      }

      FoodItem newFoodItem = new FoodItem(foodName);
      Amount amount = new Amount(quantity, unit);

      ingredients.put(newFoodItem, amount);

      ingredientsList.getItems().add(foodName + " : " + quantity + " " + unit.getSymbol());

      foodItemField.clear();
      foodItemAmountSpinner.getValueFactory().setValue(1.0);
      foodItemUnitComboBox.setValue(Unit.GRAM);
    } catch (IllegalArgumentException exception) {
      ShowAlertMessage.showErrorAlert("Ugyldig input", null, exception.getMessage());
    }
  }

  /**
   * Checks whether a food item name is valid. The name must not be blank and may only contain
   * letters and hyphens.
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
   * Adds a preparation step to the current recipe. Empty or blank steps are ignored.
   *
   * @param event the action event triggered by the add button
   */
  @FXML
  public void addStep(ActionEvent event) {
    String step = stepsField.getText();

    if (step != null && !step.isBlank()) {
      stepsList.getItems().add(step);
      stepsField.clear();
    }
  }

  /**
   * Creates and saves the recipe. Validates the recipe name, ingredients and preparation steps
   * before writing the recipe to the JSON file.
   *
   * @param event the action event triggered by the add recipe button
   */
  @FXML
  public void addRecipe(ActionEvent event) {
    String recipeName = nameField.getText();

    if (recipeName == null || recipeName.isBlank()) {
      ShowAlertMessage.showErrorAlert("Ugyldig input", null, "Oppskriften må ha et navn.");
      return;
    }

    if (ingredients.isEmpty()) {
      ShowAlertMessage.showErrorAlert("Ugyldig input", null, "Legg til minst en ingrediens.");
      return;
    }

    List<String> steps = new ArrayList<>(stepsList.getItems());

    if (steps.isEmpty()) {
      ShowAlertMessage.showErrorAlert("Ugyldig input", null, "Legg til minst ett steg.");
      return;
    }

    Recipe newRecipe =
        new Recipe(recipeName, App.getCurrentUser(), new LinkedHashMap<>(ingredients), steps);

    try {
      App.getRecipeBook().addRecipe(newRecipe);
      save();

      ShowAlertMessage.showInfoAlert("Suksess", null, "Oppskrift lagt til");
      clearForm();

    } catch (IOException e) {
      ShowAlertMessage.showErrorAlert(
          "Klarte ikke å lagre oppskriften", null, "Feilen er: " + e.getMessage());
    }
  }

  /** Clears all input fields and removes all ingredients and steps from the current recipe. */
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
  public void backToHomePage(ActionEvent event) throws IOException {
    Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

    stage.setScene(new Scene(root));
    stage.show();
  }

  /**
   * Saves the current recipe book to the JSON file. The file path is relative to the project root.
   *
   * @throws IOException if an error occurs while writing to the file
   */
  private void save() throws IOException {
    recipeFileHandler.save(Path.of("..", "data", "recipes.json"), App.getRecipeBook());
  }
}
