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

/** Controller for loading and displaying a list of saved recipes. */
public class RecipeViewPageController {
  /** List view component for displaying recipe names in the UI. */
  @FXML private ListView<String> recipesListView;

  /** Initializes the page by loading recipes from file and populating the list view. */
  @FXML
  private void initialize() {
    List<String> recipeNames =
        App.getRecipeBook().getRecipes().stream().map(r -> r.getName()).toList();
    recipesListView.getItems().setAll(recipeNames);
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
}
