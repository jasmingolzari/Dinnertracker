package no.gr2631.dinnertracker.ui;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.User;

/**
 * Controls the login page, selects or creates the current user through the REST API, and loads the
 * user's fridge and recipes before opening the home page.
 */
public class UserLogInPageController {

  /** Text field containing the username entered on the login page. */
  @FXML private TextField usernameTextField;

  private final DinnertrackerRestClient restClient;

  /** Creates the login controller using the application's shared REST client. */
  public UserLogInPageController() {
    this(App.restClient);
  }

  /**
   * Creates a login controller that uses the given REST client.
   *
   * @param restClient the client used to look up users and load their data
   */
  UserLogInPageController(DinnertrackerRestClient restClient) {
    this.restClient = restClient;
  }

  /**
   * Validates the username and logs in an existing user or registers a new user.
   *
   * <p>Everything is fetched from the server before the shared application state is changed, so a
   * failed request leaves the previous user and data untouched.
   *
   * @param event the login button action
   */
  @FXML
  public void handleLoginButtonAction(ActionEvent event) {
    String username = usernameTextField.getText();

    if (username == null || username.trim().isEmpty()) {
      ShowAlertMessage.showErrorAlert(
          "Login Error", "Brukernavn påkrevd", "Vennligst skriv inn et gyldig brukernavn.");
      return;
    }

    username = username.trim();

    try {
      User existingUser = restClient.getUser(username);
      boolean isNewUser = existingUser == null;
      User user = isNewUser ? new User(username) : existingUser;

      Fridge fridge = restClient.getFridge(user);
      RecipeBook recipeBook = restClient.getRecipeBook(user);

      if (isNewUser) {
        restClient.createUser(user);
      }

      App.setCurrentUser(user);
      App.setFridge(fridge);
      App.setRecipeBook(recipeBook);

      continueToHomePage(event);

      if (isNewUser) {
        ShowAlertMessage.showInfoAlert(
            "Bruker opprettet", null, "Ny bruker opprettet. Velkommen, " + username + "!");
      } else {
        ShowAlertMessage.showInfoAlert(
            "Login Suksessfull", null, "Velkommen tilbake, " + username + "!");
      }

    } catch (IOException e) {
      ShowAlertMessage.showErrorAlert(
          "Feil", "Kunne ikke logge inn", "En feil oppstod: " + e.getMessage());
    }
  }

  /**
   * Replaces the current scene's root with the home page.
   *
   * @param event the action used to locate the current window
   * @throws IOException if the home page FXML cannot be loaded
   */
  private void continueToHomePage(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("HomePage.fxml"));
    Parent root = loader.load();

    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.getScene().setRoot(root);
  }
}
