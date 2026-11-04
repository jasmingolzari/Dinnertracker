package no.gr2631.dinnertracker.ui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.User;
import no.gr2631.dinnertracker.persistence.FridgeFileHandler;
import no.gr2631.dinnertracker.persistence.RecipeFileHandler;
import no.gr2631.dinnertracker.persistence.UsersFileHandler;

/**
 * Entry point for the Dinnertracker JavaFX application. Holds the shared user, fridge, recipe book,
 * and persistence handlers used by the application's controllers.
 */
public class App extends Application {
  /** Currently selected user, or {@code null} before a user is selected. only for package usage. */
  private static User currentUser;

  /**
   * Current user's fridge, or {@code null} before it is loaded or assigned. only for package usage.
   */
  private static Fridge fridge;

  /**
   * Current user's recipe book, or {@code null} before it is loaded or assigned. only for package
   * usage.
   */
  private static RecipeBook recipeBook;

  private static final ReadOnlyLongWrapper fridgeVersion = new ReadOnlyLongWrapper();

  /** Shared handler for loading and saving fridge data. */
  public static final FridgeFileHandler fridgeFileHandler = new FridgeFileHandler();

  /** Shared handler for loading and saving recipe data. */
  public static final RecipeFileHandler recipeFileHandler = new RecipeFileHandler();

  public static final UsersFileHandler usersFileHandler = new UsersFileHandler();

  /**
   * Loads the home page and displays the main application window.
   *
   * @param stage the primary stage supplied by the JavaFX runtime
   * @throws Exception if the home page cannot be loaded or startup fails
   */
  @Override
  public void start(Stage stage) throws Exception {
    Parent root = FXMLLoader.load(App.class.getResource("UserLogInPage.fxml"));
    stage.setScene(new Scene(root));
    stage.setTitle("Dinnertracker");
    stage.show();
  }

  /**
   * Launches the JavaFX application.
   *
   * @param args command-line arguments passed to the JavaFX runtime
   */
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Returns the currently selected user, or {@code null} if no user is selected.
   *
   * @return the currently selected user, or {@code null} if no user is selected
   */
  public static User getCurrentUser() {
    return currentUser;
  }

  /**
   * Sets the currently selected user.
   *
   * @param currentUser the user to set as the current user
   */
  public static void setCurrentUser(User currentUser) {
    App.currentUser = currentUser;
  }

  /**
   * Returns the current user's fridge, or {@code null} if no fridge is loaded.
   *
   * @return the current user's fridge, or {@code null} if no fridge is loaded
   */
  @SuppressFBWarnings(
      value = "MS_EXPOSE_REP",
      justification = "Controllers intentionally share the current user's mutable fridge.")
  public static Fridge getFridge() {
    return fridge;
  }

  /**
   * Sets the current user's fridge.
   *
   * @param fridge the fridge to set as the current user's fridge
   */
  @SuppressFBWarnings(
      value = "EI_EXPOSE_STATIC_REP2",
      justification = "Controllers intentionally share the assigned mutable fridge.")
  public static void setFridge(Fridge fridge) {
    App.fridge = fridge;
    fridgeVersion.set(fridgeVersion.get() + 1);
  }

  /** Notifies UI controllers that the contents of the current fridge have changed. */
  public static void notifyFridgeChanged() {
    fridgeVersion.set(fridgeVersion.get() + 1);
  }

  /** Returns a read-only property that changes whenever the current fridge changes. */
  public static ReadOnlyLongProperty fridgeVersionProperty() {
    return fridgeVersion.getReadOnlyProperty();
  }

  /**
   * Returns the current user's recipe book, or {@code null} if no recipe book is loaded.
   *
   * @return the current user's recipe book, or {@code null} if no recipe book is loaded
   */
  @SuppressFBWarnings(
      value = "MS_EXPOSE_REP",
      justification = "Controllers intentionally share the current user's mutable recipe book.")
  public static RecipeBook getRecipeBook() {
    return recipeBook;
  }

  /**
   * Sets the current user's recipe book.
   *
   * @param recipeBook the recipe book to set as the current user's recipe book
   */
  @SuppressFBWarnings(
      value = "EI_EXPOSE_STATIC_REP2",
      justification = "Controllers intentionally share the assigned mutable recipe book.")
  public static void setRecipeBook(RecipeBook recipeBook) {
    App.recipeBook = recipeBook;
  }
}
