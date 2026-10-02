package no.gr2631.dinnertracker.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.core.Fridge;

public class App extends Application {
  private static final Fridge FRIDGE = new Fridge();

  /**
   * Get the fridge shared by the application pages.
   *
   * @return the application fridge
   */
  static Fridge getFridge() {
    return FRIDGE;
  }

  @Override
  public void start(Stage stage) throws Exception {
    Parent root = FXMLLoader.load(App.class.getResource("HomePage.fxml"));
    stage.setScene(new Scene(root));
    stage.setTitle("Dinnertracker");
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
