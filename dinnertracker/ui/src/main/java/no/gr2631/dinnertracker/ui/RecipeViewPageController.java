package no.gr2631.dinnertracker.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.User;

/** Controller for loading and displaying a list of saved recipes. */
public class RecipeViewPageController {
  /** List view component for displaying recipe names in the UI. */
  @FXML private ListView<String> recipesListView;

  private List<Recipe> recipes;

  private Map<FoodItem, Amount> fridgeContents;

  /**
   * Loads recipes and sorts them by how many ingredients are still needed.
   *
   * <p>Recipes that can be made with the current fridge contents appear first. The remaining
   * recipes are ordered by the number of missing ingredients and then by name.
   */
  @FXML
  public void initialize() {
    App.fridgeVersionProperty().addListener((observable, oldValue, newValue) -> refreshRecipes());
    recipesListView.setCellFactory(
        listView -> {
          return new ListCell<>() {
            @Override
            protected void updateItem(String recipeName, boolean empty) {
              super.updateItem(recipeName, empty);
              if (empty || recipeName == null) {
                setText(null);
                setTextFill(null);
                setStyle("");
                return;
              }

              setStyle("-fx-cursor: hand;");
              // Look the recipe up by position, not by name: shared recipes from different users
              // can have the same name.
              Recipe recipe = recipeAt(getIndex());
              if (recipe == null) {
                setText(recipeName);
                setTextFill(Color.web("#2F6B3B"));
                return;
              }

              int ingredientsLeft = numberOfIngredientsLeft(recipe);
              int needed = recipe.getIngredients().size();
              int available = needed - ingredientsLeft;
              setText(
                  recipeName
                      + " ("
                      + available
                      + "/"
                      + needed
                      + " ingredients)"
                      + addedByText(recipe));
              setTextFill(available == needed ? Color.web("#2F6B3B") : Color.web("#6B8F71"));
            }
          };
        });
    refreshRecipes();
    // Catches a click on wanted recipe to load its details
    recipesListView.setOnMouseClicked(event -> openRecipeDetails());
  }

  /** Reloads recipe availability and refreshes the displayed ingredient indicators. */
  private void refreshRecipes() {
    if (App.getRecipeBook() == null || App.getFridge() == null) {
      return;
    }

    recipes = new ArrayList<>(App.getRecipeBook().getRecipes());
    fridgeContents = App.getFridge().getFoodItems();
    recipes.sort(
        Comparator.comparingInt((Recipe recipe) -> canMakeRecipe(recipe) ? 1 : 0)
            .reversed()
            .thenComparing(Comparator.comparingInt(this::numberOfIngredientsLeft))
            .thenComparing(recipe -> recipe.getName()));

    List<String> recipeNames = recipes.stream().map(recipe -> recipe.getName()).toList();
    recipesListView.getItems().setAll(recipeNames);
    recipesListView.refresh();
  }

  /**
   * Returns the recipe shown at the given position in the list.
   *
   * @param index the list position
   * @return the recipe at that position, or {@code null} if there is none
   */
  private Recipe recipeAt(int index) {
    if (recipes == null || index < 0 || index >= recipes.size()) {
      return null;
    }
    return recipes.get(index);
  }

  /**
   * Describes who added a recipe, so shared recipes from other users can be told apart.
   *
   * @param recipe the recipe to describe
   * @return {@code " – lagt til av <navn>"} for other users' recipes, otherwise an empty string
   */
  private static String addedByText(Recipe recipe) {
    User owner = recipe.getUserName();
    User currentUser = App.getCurrentUser();
    if (owner == null
        || owner.getUserName() == null
        || (currentUser != null && owner.getUserName().equals(currentUser.getUserName()))) {
      return "";
    }
    return " – lagt til av " + owner.getUserName();
  }

  private boolean canMakeRecipe(Recipe recipe) {
    return numberOfIngredientsLeft(recipe) == 0;
  }

  private int numberOfIngredientsLeft(Recipe recipe) {
    int ingredientsLeft = 0;

    for (Map.Entry<FoodItem, Amount> ingredient : recipe.getIngredients().entrySet()) {
      Amount available = fridgeContents.get(ingredient.getKey());
      Amount required = ingredient.getValue();

      if (available == null
          || available.unit() != required.unit()
          || available.quantity() < required.quantity()) {
        ingredientsLeft++;
      }
    }

    return ingredientsLeft;
  }

  /**
   * Navigates the user to the recipe creation page.
   *
   * @param event the event triggering the navigation
   * @throws IOException if the FXML file cannot be loaded
   */
  @FXML
  public void makeNewRecipe(ActionEvent event) throws IOException {
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
  public void backToHomePage(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("HomePage.fxml"));
    Parent root = loader.load();

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.getScene().setRoot(root);
  }

  /** Opens the details page for the recipe selected in the list view. */
  @FXML
  private void openRecipeDetails() {
    // Use the selected position, not the name: two shared recipes can have the same name.
    Recipe clickedRecipe = recipeAt(recipesListView.getSelectionModel().getSelectedIndex());
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
    } catch (IOException e) {
      ShowAlertMessage.showErrorAlert("Feil", "Kunne ikke åpne oppskrift.", e.getMessage());
    }
  }
}
