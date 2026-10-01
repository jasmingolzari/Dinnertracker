package no.ntnu.it1901.gr2631.dinnertracker.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import no.ntnu.it1901.gr2631.dinnertracker.core.Amount;
import no.ntnu.it1901.gr2631.dinnertracker.core.FoodItem;
import no.ntnu.it1901.gr2631.dinnertracker.core.Recipe;
import no.ntnu.it1901.gr2631.dinnertracker.core.Unit;

class RecipeFileHandlerTest {

    private RecipeFileHandler handler;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        handler = new RecipeFileHandler();
    }

    private Path createFile(String fileName, String content) throws IOException {
        Path path = tempDir.resolve(fileName);
        Files.writeString(path, content);
        return path;
    }

    private Recipe createRecipe(String name) {
        Recipe recipe = new Recipe(name);
        recipe.addIngredient(
                new FoodItem("Mel"),
                new Amount(500, Unit.GRAM));
        recipe.addStep("Bland ingrediensene");
        recipe.addStep("Stek i ovnen");

        return recipe;
    }

    @Test
    void loadAllShouldReturnEmptyListWhenFileDoesNotExist() throws IOException {
        Path path = tempDir.resolve("recipes.json");

        List<Recipe> recipes = handler.loadAll(path);

        assertTrue(recipes.isEmpty());
    }

    @Test
    void loadAllShouldReturnEmptyListWhenFileIsEmpty() throws IOException {
        Path path = createFile("recipes.json", "");

        List<Recipe> recipes = handler.loadAll(path);

        assertTrue(recipes.isEmpty());
    }

    @Test
    void saveAllAndLoadAllShouldPreserveRecipe() throws IOException {
        Path path = tempDir.resolve("recipes.json");
        Recipe recipe = createRecipe("Pannekaker");

        handler.saveAll(List.of(recipe), path);

        List<Recipe> recipes = handler.loadAll(path);

        assertEquals(1, recipes.size());
        assertEquals("Pannekaker", recipes.get(0).getName());
        assertEquals(2, recipes.get(0).getSteps().size());
        assertEquals(1, recipes.get(0).getIngredients().size());
    }

    @Test
    void saveAllShouldOverwriteExistingRecipes() throws IOException {
        Path path = tempDir.resolve("recipes.json");

        handler.saveAll(List.of(createRecipe("Gammel oppskrift")), path);
        handler.saveAll(List.of(createRecipe("Ny oppskrift")), path);

        List<Recipe> recipes = handler.loadAll(path);

        assertEquals(1, recipes.size());
        assertEquals("Ny oppskrift", recipes.get(0).getName());
    }

    @Test
    void addRecipeShouldKeepExistingRecipes() throws IOException {
        Path path = tempDir.resolve("recipes.json");

        handler.addRecipe(createRecipe("Pannekaker"), path);
        handler.addRecipe(createRecipe("Pizza"), path);

        List<Recipe> recipes = handler.loadAll(path);

        assertEquals(2, recipes.size());
        assertEquals("Pannekaker", recipes.get(0).getName());
        assertEquals("Pizza", recipes.get(1).getName());
    }

    @Test
    void loadRecipeShouldReturnRecipeWithMatchingName() throws IOException {
        Path path = tempDir.resolve("recipes.json");

        handler.saveAll(
                List.of(
                        createRecipe("Pannekaker"),
                        createRecipe("Pizza")),
                path);

        Recipe recipe = handler.loadRecipe(path, "Pizza");

        assertEquals("Pizza", recipe.getName());
    }

    @Test
    void loadRecipeShouldThrowExceptionWhenRecipeDoesNotExist() throws IOException {
        Path path = tempDir.resolve("recipes.json");

        handler.saveAll(List.of(createRecipe("Pizza")), path);

        assertThrows(
                IllegalArgumentException.class,
                () -> handler.loadRecipe(path, "Pannekaker"));
    }

    @Test
    void loadAllShouldThrowExceptionForInvalidJson() throws IOException {
        Path path = createFile("recipes.json", "Dette er ikke gyldig JSON");

        assertThrows(
                IOException.class,
                () -> handler.loadAll(path));
    }

    @Test
    void loadAllShouldPreserveIngredientAmountAndUnit() throws IOException {
        Path path = tempDir.resolve("recipes.json");

        Recipe recipe = createRecipe("Pannekaker");
        handler.saveAll(List.of(recipe), path);

        Recipe loaded = handler.loadAll(path).get(0);

        FoodItem flour = loaded.getIngredients().keySet().iterator().next();
        Amount amount = loaded.getIngredients().get(flour);

        assertEquals("Mel", flour.getName());
        assertEquals(500, amount.quantity());
        assertEquals(Unit.GRAM, amount.unit());
    }
}