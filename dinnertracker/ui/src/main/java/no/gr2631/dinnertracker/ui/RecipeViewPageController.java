package no.gr2631.dinnertracker.ui;

import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.core.Recipe;

/** Controller for loading and displaying a list of saved recipes. */
public class RecipeViewPageController {
  /** List view component for displaying recipe names in the UI. */
  @FXML private ListView<String> recipesListView;

  private List<Recipe> recipes;

  /** Initializes the page by loading recipes from file and populating the list view. */
  @FXML
  private void initialize() {
    recipes = App.getRecipeBook().getRecipes();
    List<String> recipeNames = recipes.stream().map(Recipe::getName).toList();
    recipesListView.getItems().setAll(recipeNames);
    // Catches a click on wanted recipe to load its details
    recipesListView.setOnMouseClicked(event -> openRecipeDetails());
  }

  /**
   * Navigates the user to the recipe creation page.
   *
   * @param event the event triggering the navigation
   * @throws IOException if the FXML file cannot be loaded
   */
  @FXML
  private void makeNewRecipe(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("RecipePage.fxml"));
    Parent root = loader.load();

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.getScene().setRoot(root);
  }

  /**
   * Navigates the user back to the home page.
   *
   * @param event the event triggering the navigation
   * @throws IOException if the FXML file cannot be loaded
   */
  @FXML
  private void backToHomePage(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("HomePage.fxml"));
    Parent root = loader.load();

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.getScene().setRoot(root);
  }

  /**
   * TODO: Method for when clicking a recipe from listView on RecipeViewPage it directs to this
   * object - Will load RecipeDetailsViewPage.fxml - Send the clicked recipe object to
   * RecipeDetailsViewPage.fxml controller
   */
  @FXML
  private void openRecipeDetails() {
    String clickedName = recipesListView.getSelectionModel().getSelectedItem();
    if (clickedName == null) {
      return;
    }

    Recipe clickedRecipe = null;
    for (Recipe recipe : recipes) {
      if (recipe.getName().equals(clickedName)) {
        clickedRecipe = recipe;
        break;
      }
    }

    if (clickedRecipe == null) {
      return;
    }

    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("RecipeDetailsViewPage.fxml"));

      Parent root = loader.load();
      RecipeDetailsViewPageController controller = loader.getController();
      controller.setRecipe(clickedRecipe);

      Stage stage = (Stage) recipesListView.getScene().getWindow();
      stage.getScene().setRoot(root);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
