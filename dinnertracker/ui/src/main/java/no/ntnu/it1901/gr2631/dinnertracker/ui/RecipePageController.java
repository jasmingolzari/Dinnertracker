package no.ntnu.it1901.gr2631.dinnertracker.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import no.ntnu.it1901.gr2631.dinnertracker.core.FoodItem;
import no.ntnu.it1901.gr2631.dinnertracker.core.Recipe;
import no.ntnu.it1901.gr2631.dinnertracker.persistence.RecipeFileHandler;

public class RecipePageController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField foodItemField;

    @FXML
    private TextField foodItemAmountField;

    @FXML
    private Button addFoodItemButton;

    @FXML
    private ListView<String> ingredientsList;

    @FXML
    private TextField stepsField;

    @FXML
    private Button addStepButton;

    @FXML
    private ListView<String> stepsList;

    @FXML
    private Button backButton;

    @FXML
    private Button addRecipeButton;

    private  final List<FoodItem> foodItems = new ArrayList<>();
    @FXML
    private void addFoodItem(ActionEvent event) {
        String foodName = foodItemField.getText(); 
        String amount = foodItemAmountField.getText();

        if (!isValidFoodItemName(foodName)) {
            showAlertMessage("Ugyldig navn på matvare. Kun bokstaver er tillat.");
            return;
        }
        
        FoodItem newFoodItem = new FoodItem(foodName); // Kan fortsette her til neste release 
        foodItems.add(newFoodItem); 
        ingredientsList.getItems().add(foodName + "-" + amount); 

        foodItemField.clear();
        foodItemAmountField.clear();
    }

    private boolean isValidFoodItemName(String foodName){
        if (foodName == null || foodName.isBlank()) {
            return false; 
        }

        for (char c  : foodName.toCharArray()) {
            if (!Character.isLetter(c) && c != '-') {
                return false; 
            }
        }
        return true; 
    }

    private void showAlertMessage(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR); 
        alert.setTitle("Ugyldig input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait(); 
    }

    @FXML
    private void addStep(ActionEvent event) {
        String step = stepsField.getText();

        if (step != null && !step.isBlank()) {
            stepsList.getItems().add(step);
            stepsField.clear();
        }
    }

    @FXML
    private void addRecipe(ActionEvent event) {
        String recipeName = nameField.getText();
        
        if(recipeName == null || recipeName.isBlank()) {
            showAlertMessage("Oppskriften må ha et navn.");
            return;
        }

        if(foodItems.isEmpty()) {
            showAlertMessage("Legg til minst en ingrediens");
            return;
        }

        List<String> steps = new ArrayList<>(stepsList.getItems()); 
        if (steps.isEmpty()) {
            showAlertMessage("Legg til minst ett steg");
            return; 
        }

        Recipe newRecipe = new Recipe(recipeName, new ArrayList<>(foodItems), steps);

 
        Path filePath = Path.of("..", "data", "recipes.json");
        RecipeFileHandler fileHandler = new RecipeFileHandler(); 
        try {
            fileHandler.addRecipe(newRecipe, filePath);

            showAddedRecipeMessage("Oppskrift lagt til");
            clearForm();


        } catch (IOException e) {
            showAlertMessage("Klarte ikke å lagre oppskriften: " + e.getMessage());
        }
        clearForm();
    }
    
    private void showAddedRecipeMessage(String message){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Suksess");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm(){
        nameField.clear();
        foodItemField.clear();
        foodItemAmountField.clear();
        stepsField.clear();
        ingredientsList.getItems().clear();
        stepsList.getItems().clear();
        foodItems.clear();
    }

    @FXML
    private void backToHomePage(ActionEvent event) throws IOException {
        Parent root;
        root = FXMLLoader.load(
                getClass().getResource("HomePage.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
        .getScene().getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }
}