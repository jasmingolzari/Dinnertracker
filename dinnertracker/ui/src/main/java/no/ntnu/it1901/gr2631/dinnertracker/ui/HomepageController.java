package no.ntnu.it1901.gr2631.dinnertracker.ui;

import javafx.fxml.FXML;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;


public class HomepageController {
    @FXML
    private void openRecipe(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
            getClass().getResource("/fxml/recipe.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
        .getScene().getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }

     @FXML
    private void openFridge(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
            getClass().getResource("/fxml/fridge.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
        .getScene().getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }
}
