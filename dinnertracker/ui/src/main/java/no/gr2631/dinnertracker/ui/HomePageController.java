package no.gr2631.dinnertracker.ui;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controller for the homepage
 *
 * <p>Handles navigation through pages from homepage.
 */
public class HomePageController {

  /**
   * Opens recipe page when button "Oppskrifter" is clicked on.
   *
   * @param event the action event triggered by clicking the button.
   * @throws IOException if recipe page FXML cannot be loaded.
   */
  @FXML
  public void openRecipePage(ActionEvent event) throws IOException {
    Parent root = FXMLLoader.load(getClass().getResource("RecipeViewPage.fxml"));

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

    stage.setScene(new Scene(root));
    stage.show();
  }

  /**
   * Opens fridge page when button "Kjøleskap" is clicked on.
   *
   * @param event the action event triggered by clicking the button.
   * @throws IOException if fridge page FXML cannot be loaded.
   */
  @FXML
  public void openFridgePage(ActionEvent event) throws IOException {
    Parent root = FXMLLoader.load(getClass().getResource("FridgePage.fxml"));

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

    stage.setScene(new Scene(root));
    stage.show();
  }
}
