package no.ntnu.it1901.gr2631.dinnertracker.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import no.ntnu.it1901.gr2631.dinnertracker.core.FoodItem;
import no.ntnu.it1901.gr2631.dinnertracker.core.Recipe;

public class RecipeFileHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveRecipe() throws Exception {
        Recipe recipe = new Recipe("Pasta med tomat");
        recipe.addIngridient(new FoodItem("Pasta"));
        recipe.addIngridient(new FoodItem("Tomat"));

        Path file = tempDir.resolve("recipe.json");

        RecipeFileHandler fileHandler = new RecipeFileHandler();
        fileHandler.saveRecipe(recipe, file);

        String json = Files.readString(file);

        assertTrue(json.contains("Pasta med tomat"));
        assertTrue(json.contains("Pasta"));
        assertTrue(json.contains("Tomat"));
    }

    @Test
    void testLoadRecipe() throws Exception {
        Recipe originalRecipe = new Recipe("Pasta med tomat");
        originalRecipe.addIngridient(new FoodItem("Pasta"));
        originalRecipe.addIngridient(new FoodItem("Tomat"));
        originalRecipe.addStep("Kok pastaen");
        originalRecipe.addStep("Tilsett tomat");

        Path file = tempDir.resolve("recipe.json");

        RecipeFileHandler fileHandler = new RecipeFileHandler();
        fileHandler.saveRecipe(originalRecipe, file);

        Recipe loadedRecipe = fileHandler.loadRecipe(file);

        assertEquals("Pasta med tomat", loadedRecipe.getName());
        assertEquals(2, loadedRecipe.getIngridients().size());
        assertEquals("Pasta", loadedRecipe.getIngridients().get(0).getName());
        assertEquals("Tomat", loadedRecipe.getIngridients().get(1).getName());
        assertEquals(2, loadedRecipe.getSteps().size());
        assertEquals("Kok pastaen", loadedRecipe.getSteps().get(0));
        assertEquals("Tilsett tomat", loadedRecipe.getSteps().get(1));
    }
}