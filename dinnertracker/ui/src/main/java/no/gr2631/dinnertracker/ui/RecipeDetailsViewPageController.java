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

public class RecipeDetailsViewPageController {
  /** Fields that are needeed to get information from recipe.json */
  @FXML private Label recipeNameLabel;

  @FXML private VBox ingredientListVBox;

  @FXML private VBox stepsListVBox;

  private Recipe recipe;

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "The controller intentionally shares the selected recipe.")
  public void setRecipe(Recipe recipe) {
    this.recipe = recipe;
    setRecipeName(recipe);
    setRecipeSteps(recipe);
    setRecipeIngredients(recipe);
  }

  /** Method to get recipe name from json file loads to its label recipeNameLabel */
  private void setRecipeName(Recipe recipe) {
    recipeNameLabel.setText(recipe.getName());
  }

  /**
   * Method to get ingridient list from json for every new line: make new checkbox for every other
   * checkbox, change color to white/light green
   */
  private void setRecipeSteps(Recipe recipe) {
    List<String> recipeSteps = recipe.getSteps();
    for (String recipeStep : recipeSteps) {
      Label label = new Label("   - " + recipeStep);
      stepsListVBox.getChildren().add(label);
    }
  }

  /** TODO: Method to get steps from json for every new step line: start line with - */
  private void setRecipeIngredients(Recipe recipe) {
    Map<FoodItem, Amount> ingredients = recipe.getIngredients();
    int index = 0;

    for (Map.Entry<FoodItem, Amount> entry : ingredients.entrySet()) {
      FoodItem foodItem = entry.getKey();
      Amount amount = entry.getValue();

      String text = amount.quantity() + " " + amount.unit().getSymbol() + " " + foodItem.getName();
      CheckBox checkBox = new CheckBox(text);
      checkBox.setMaxWidth(Double.MAX_VALUE);

      if (index % 2 == 0) {
        checkBox.setStyle("-fx-background-color: #FFFFFF");
      } else {
        checkBox.setStyle("-fx-background-color: #E8F5E9");
      }

      ingredientListVBox.getChildren().add(checkBox);
      index++;
    }
  }

  /**
   * Method to "use recipe" and connect fridge to this recipe recipe can only be used if fridge
   * contains all listed ingredients - goes through all ingredients in ingredientListVBox and
   * compares it to what is in fridge - if an ingredient is not in fridge, error, cannot use this
   * recipe - updates what is in the fridge (removes what is in the recipe)
   */
  @FXML
  private void handleUseRecipe() {
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

  /**
   * Method for when use recipe has been clicked and passed all checkboxes get checked, becuase all
   * ingredients are in fridge
   */

  /** Method for checking off all checkboxes if use recipe can if use recipe can be used */

  /** Brings user back to list view page */
  @FXML
  private void backToViewPage(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("RecipeViewPage.fxml"));
    Parent root = loader.load();

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.getScene().setRoot(root);
  }

  /** Method for checking of checkboxes if use recipe button is used */
  @FXML
  private void checkAllIngredients() {
    for (Node node : ingredientListVBox.getChildren()) {
      if (node instanceof CheckBox checkBox) {
        checkBox.setSelected(true);
      }
    }
  }
}
