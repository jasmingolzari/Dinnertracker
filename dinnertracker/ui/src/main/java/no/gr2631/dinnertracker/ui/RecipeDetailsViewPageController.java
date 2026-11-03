package no.gr2631.dinnertracker.ui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.Recipe;

/** Controller for displaying a selected recipe's name, ingredients, and preparation steps. */
public class RecipeDetailsViewPageController {
  /** Label that displays the selected recipe's name. */
  @FXML private Label recipeNameLabel;

  /** Container for the selected recipe's ingredients. */
  @FXML private VBox ingredientListVBox;

  /** Container for the selected recipe's numbered preparation steps. */
  @FXML private VBox stepsListVBox;

  private Recipe recipe;

  /**
   * Sets the selected recipe and populates the name, ingredients, and preparation steps.
   *
   * @param recipe the recipe to display
   */
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "The controller intentionally shares the selected recipe.")
  public void setRecipe(Recipe recipe) {
    this.recipe = recipe;
    setRecipeName(recipe);
    setIngredientAvailability(recipe);
    setRecipeSteps(recipe);
    setRecipeIngredients(recipe);
  }

  /**
   * Displays how many of the recipe's ingredients are currently available in the fridge.
   *
   * <p>Fully available recipes use the application's dark green accent color, while incomplete
   * recipes use a softer green.
   *
   * @param recipe the recipe whose ingredient availability should be displayed
   */
  private void setIngredientAvailability(Recipe recipe) {
    Fridge fridge = App.getFridge();
    Map<FoodItem, Amount> fridgeContents = fridge == null ? Map.of() : fridge.getFoodItems();
    int availableIngredients = 0;

    for (Map.Entry<FoodItem, Amount> ingredient : recipe.getIngredients().entrySet()) {
      if (isIngredientAvailable(ingredient, fridgeContents)) {
        availableIngredients++;
      }
    }

    int requiredIngredients = recipe.getIngredients().size();
    Label availabilityLabel =
        new Label(
            "Du har " + availableIngredients + " av " + requiredIngredients + " ingredienser");
    availabilityLabel.setStyle(
        availableIngredients == requiredIngredients
            ? "-fx-text-fill: #2F6B3B; -fx-font-weight: bold;"
            : "-fx-text-fill: #6B8F71;");
    ingredientListVBox.getChildren().add(availabilityLabel);
  }

  private boolean isIngredientAvailable(
      Map.Entry<FoodItem, Amount> ingredient, Map<FoodItem, Amount> fridgeContents) {
    Amount available = fridgeContents.get(ingredient.getKey());
    Amount required = ingredient.getValue();
    return available != null
        && available.unit() == required.unit()
        && available.quantity() >= required.quantity();
  }

  /**
   * Displays the recipe name in the recipe name label.
   *
   * @param recipe the recipe whose name should be displayed
   */
  private void setRecipeName(Recipe recipe) {
    recipeNameLabel.setText(recipe.getName());
  }

  /**
   * Displays the recipe's preparation steps in the steps container.
   *
   * <p>Each step is displayed as a separate label and numbered according to its position in the
   * recipe.
   *
   * @param recipe the recipe whose preparation steps should be displayed
   */
  private void setRecipeSteps(Recipe recipe) {
    List<String> recipeSteps = recipe.getSteps();

    for (int i = 0; i < recipeSteps.size(); i++) {
      Label label = new Label((i + 1) + ". " + recipeSteps.get(i));
      stepsListVBox.getChildren().add(label);
    }
  }

  /**
   * Displays each recipe ingredient as a checkbox, highlighting ingredients available in the
   * fridge.
   *
   * @param recipe the recipe whose ingredients should be displayed
   */
  private void setRecipeIngredients(Recipe recipe) {
    Map<FoodItem, Amount> ingredients = recipe.getIngredients();
    Fridge fridge = App.getFridge();
    Map<FoodItem, Amount> fridgeContents = fridge == null ? Map.of() : fridge.getFoodItems();

    for (Map.Entry<FoodItem, Amount> entry : ingredients.entrySet()) {
      FoodItem foodItem = entry.getKey();
      Amount amount = entry.getValue();

      String text = amount.quantity() + " " + amount.unit().getSymbol() + " " + foodItem.getName();
      CheckBox checkBox = new CheckBox(text);
      checkBox.setMaxWidth(Double.MAX_VALUE);

      checkBox.setStyle(
          isIngredientAvailable(entry, fridgeContents)
              ? "-fx-background-color: #E8F5E9; -fx-cursor: hand;"
              : "-fx-background-color: #FFFFFF; -fx-cursor: hand;");

      ingredientListVBox.getChildren().add(checkBox);
    }
  }

  /**
   * Uses the selected recipe, removes its ingredients from the current fridge, and saves the
   * updated fridge.
   */
  @FXML
  public void handleUseRecipe() {
    if (recipe == null) {
      return;
    }

    Path path = Path.of("..", "data", "fridges.json");
    Fridge fridge = App.getFridge();
    if (fridge == null) {
      ShowAlertMessage.showErrorAlert("Feil.", "Ingen kjøleskap funnet.", "");
      return;
    }

    try {
      App.getRecipeBook().useRecipe(recipe, fridge);

      App.fridgeFileHandler.save(path, fridge);
      checkAllIngredients();

      ShowAlertMessage.showInfoAlert("Oppskrift brukt", "Ingredienser fjernet fra kjøleskap.", "");

    } catch (IOException e) {
      ShowAlertMessage.showErrorAlert("Feil", "Kunne ikke lagre kjøleskapet.", e.getMessage());

    } catch (IllegalArgumentException e) {
      ShowAlertMessage.showErrorAlert(
          "Kan ikke bruke oppskrift.", "Mangler ingredienser.", e.getMessage());
    }
  }

  /** Returns the user to the recipe list page. */
  @FXML
  public void backToViewPage(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("RecipeViewPage.fxml"));
    Parent root = loader.load();

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.getScene().setRoot(root);
  }

  /** Marks all displayed ingredient checkboxes as selected after a recipe is used. */
  @FXML
  private void checkAllIngredients() {
    for (Node node : ingredientListVBox.getChildren()) {
      if (node instanceof CheckBox checkBox) {
        checkBox.setSelected(true);
      }
    }
  }
}
