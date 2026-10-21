package no.gr2631.dinnertracker.ui;

import java.io.IOException;
import java.nio.file.Path;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.core.User;

/**
 * Controls the login page, selects or creates the current user, and loads the user's fridge and
 * recipes before opening the home page.
 */
public class UserLogInPageController {

  /** Text field containing the username entered on the login page. */
  @FXML private TextField usernameTextField;

  private final Path usersPath;
  private final Path fridgesPath;
  private final Path recipesPath;

  /** Creates the login controller using the application data files. */
  public UserLogInPageController() {
    this(
        Path.of("..", "data", "users.json"),
        Path.of("..", "data", "fridges.json"),
        Path.of("..", "data", "recipes.json"));
  }

  /**
   * Creates a login controller with custom persistence paths.
   *
   * @param usersPath path to the registered users file
   * @param fridgesPath path to the fridge data file
   * @param recipesPath path to the recipe data file
   */
  UserLogInPageController(Path usersPath, Path fridgesPath, Path recipesPath) {
    this.usersPath = usersPath;
    this.fridgesPath = fridgesPath;
    this.recipesPath = recipesPath;
  }

  /**
   * Validates the username and logs in an existing user or registers a new user.
   *
   * @param event the login button action
   */
  @FXML
  private void handleLoginButtonAction(ActionEvent event) {
    String username = usernameTextField.getText();

    if (username == null || username.trim().isEmpty()) {
      ShowAlertMessage.showErrorAlert(
          "Login Error", "Brukernavn påkrevd", "Vennligst skriv inn et gyldig brukernavn.");
      return;
    }

    username = username.trim();
    User requestedUser = new User(username);

    try {
      User existingUser = App.usersFileHandler.load(usersPath, requestedUser);
      boolean isNewUser = existingUser == null;
      User user = isNewUser ? requestedUser : existingUser;

      loadData(user);

      App.setCurrentUser(user);

      if (isNewUser) {
        App.usersFileHandler.save(usersPath, user);
      }

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
   * Loads the given user's fridge and recipe book into the application's shared state.
   *
   * @param userName the user whose data should be loaded
   * @throws IOException if the fridge or recipes cannot be loaded
   */
  private void loadData(User userName) throws IOException {
    App.setFridge(App.fridgeFileHandler.load(fridgesPath, userName));

    App.setRecipeBook(App.recipeFileHandler.load(recipesPath, userName));
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
