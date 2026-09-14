package no.ntnu.it1901.gr2631.dinnertracker.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(
            App.class.getResource("HomePage.fxml")
        );
        stage.setScene(new Scene(root));
        stage.setTitle("Oppskrift");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
