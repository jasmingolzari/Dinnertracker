package no.ntnu.it1901.gr2631.dinnertracker.ui;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class HomePageController {

    @FXML
    private void openRecipePage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
            getClass().getResource("RecipeViewPage.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
        .getScene().getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }

     @FXML
    private void openFridgePage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
            getClass().getResource("FridgePage.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
        .getScene().getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }
}
