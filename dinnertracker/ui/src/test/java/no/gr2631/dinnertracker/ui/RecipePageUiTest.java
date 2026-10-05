package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * UI tests for the recipe page. The tests open the page in a real window and click the buttons like
 * a user would. The recipe file is backed up before each test and restored afterwards.
 */
@ExtendWith(ApplicationExtension.class)
class RecipePageUiTest {

  private static final Path FILE = Path.of("..", "data", "recipes.json");
  private static final String BAD_FOOD_NAME =
      "Ugyldig navn på matvare. Kun bokstaver og bindestrek er tillatt.";

  private Stage stage;
  private Parent originalRoot;
  private byte[] fileBackup;

  /**
   * Loads the recipe page and shows it. Called by TestFX before each test.
   *
   * @param stage the stage given by TestFX
   * @throws IOException if the FXML file cannot be loaded
   */
  @Start
  void start(Stage stage) throws IOException {
    this.stage = stage;
    originalRoot = FXMLLoader.load(RecipePageController.class.getResource("RecipePage.fxml"));
    stage.setScene(new Scene(originalRoot));
    stage.show();
  }

  /**
   * Backs up the recipe file.
   *
   * @throws IOException if the file cannot be read
   */
  @BeforeEach
  void backUpFile() throws IOException {
    fileBackup = Files.exists(FILE) ? Files.readAllBytes(FILE) : null;
  }

  /**
   * Restores the recipe file to what it was before the test.
   *
   * @throws IOException if the file cannot be written
   */
  @AfterEach
  void restoreFile() throws IOException {
    if (fileBackup == null) {
      Files.deleteIfExists(FILE);
    } else {
      Files.write(FILE, fileBackup);
    }
  }

  /** Clicks a field and types text into it. */
  private void type(FxRobot robot, String id, String text) {
    robot.clickOn(id).write(text);
  }

  /** Returns the text field with the given id. */
  private TextField field(FxRobot robot, String id) {
    return robot.lookup(id).queryAs(TextField.class);
  }

  /** Returns the list view with the given id. */
  private ListView<?> list(FxRobot robot, String id) {
    return robot.lookup(id).queryListView();
  }

  /** Reads the message of the open alert and closes it with the enter key. */
  private String closeAlert(FxRobot robot) {
    String message = robot.lookup(".dialog-pane").queryAs(DialogPane.class).getContentText();
    robot.type(KeyCode.ENTER);
    return message;
  }

  /** Fills in a complete recipe, but does not press the add recipe button. */
  private void fillRecipe(FxRobot robot) {
    type(robot, "#nameField", "Pasta carbonara");
    type(robot, "#foodItemField", "Pasta");
    robot.clickOn("#addFoodItemButton");
    type(robot, "#stepsField", "Boil the pasta");
    robot.clickOn("#addStepButton");
  }

  /** The add ingredient button should add the ingredient to the list and empty the field. */
  @Test
  void addFoodItemButtonShouldAddIngredient(FxRobot robot) {
    type(robot, "#foodItemField", "Tomat");
    robot.clickOn("#addFoodItemButton");

    assertEquals(1, list(robot, "#ingredientsList").getItems().size());
    assertEquals("", field(robot, "#foodItemField").getText());
  }

  /** The add ingredient button should show an error when the name is empty. */
  @Test
  void addFoodItemButtonShouldShowErrorWhenNameIsEmpty(FxRobot robot) {
    robot.clickOn("#addFoodItemButton");

    assertEquals(BAD_FOOD_NAME, closeAlert(robot));
    assertTrue(list(robot, "#ingredientsList").getItems().isEmpty());
  }

  /** The add step button should add steps in order and empty the field. */
  @Test
  void addStepButtonShouldAddStepsInOrder(FxRobot robot) {
    type(robot, "#stepsField", "First");
    robot.clickOn("#addStepButton");
    type(robot, "#stepsField", "Second");
    robot.clickOn("#addStepButton");

    assertEquals("[First, Second]", list(robot, "#stepsList").getItems().toString());
    assertEquals("", field(robot, "#stepsField").getText());
  }

  /** The add recipe button should show an error when the title is empty. */
  @Test
  void addRecipeButtonShouldShowErrorWhenTitleIsEmpty(FxRobot robot) {
    robot.clickOn("#addRecipeButton");

    assertEquals("Oppskriften må ha et navn.", closeAlert(robot));
  }

  /** The add recipe button should save the recipe, show a message and clear the form. */
  @Test
  void addRecipeButtonShouldSaveRecipeAndClearForm(FxRobot robot) throws IOException {
    fillRecipe(robot);

    robot.clickOn("#addRecipeButton");

    assertEquals("Oppskrift lagt til", closeAlert(robot));
    assertTrue(Files.readString(FILE).contains("Pasta carbonara"));
    assertEquals("", field(robot, "#nameField").getText());
    assertTrue(list(robot, "#ingredientsList").getItems().isEmpty());
    assertTrue(list(robot, "#stepsList").getItems().isEmpty());
  }

  /** The back button should replace the page with the home page. */
  @Test
  void backButtonShouldGoToHomePage(FxRobot robot) {
    robot.clickOn("#backButton");

    assertNotSame(originalRoot, stage.getScene().getRoot());
  }
}
