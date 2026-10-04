package no.gr2631.dinnertracker.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests reading and writing recipes with {@link RecipeFileHandler}. */
class RecipeFileHandlerTest {

  private RecipeFileHandler handler;

  @TempDir Path tempDir;

  @BeforeEach
  /** Creates a fresh file handler before each test. */
  void setUp() {
    handler = new RecipeFileHandler();
  }

  /** Creates a file in the temporary test directory with the given content. */
  private Path createFile(String fileName, String content) throws IOException {
    Path path = tempDir.resolve(fileName);
    Files.writeString(path, content);
    return path;
  }

  /** Creates a recipe containing one ingredient and two preparation steps. */
  private Recipe createRecipe(String name) {
    Recipe recipe = new Recipe(name);
    recipe.addIngredient(new FoodItem("Mel"), new Amount(500, Unit.GRAM));
    recipe.addStep("Bland ingrediensene");
    recipe.addStep("Stek i ovnen");

    return recipe;
  }

  @Test
  /** Verifies that loading a missing file returns an empty list. */
  void loadAllShouldReturnEmptyListWhenFileDoesNotExist() throws IOException {
    Path path = tempDir.resolve("recipes.json");

    List<Recipe> recipes = handler.loadAll(path);

    assertTrue(recipes.isEmpty());
  }

  @Test
  /** Verifies that loading an empty file returns an empty list. */
  void loadAllShouldReturnEmptyListWhenFileIsEmpty() throws IOException {
    Path path = createFile("recipes.json", "");

    List<Recipe> recipes = handler.loadAll(path);

    assertTrue(recipes.isEmpty());
  }

  @Test
  /** Verifies that saving and loading preserves the recipe data. */
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
  /** Verifies that saving recipes replaces existing file contents. */
  void saveAllShouldOverwriteExistingRecipes() throws IOException {
    Path path = tempDir.resolve("recipes.json");

    handler.saveAll(List.of(createRecipe("Gammel oppskrift")), path);
    handler.saveAll(List.of(createRecipe("Ny oppskrift")), path);

    List<Recipe> recipes = handler.loadAll(path);

    assertEquals(1, recipes.size());
    assertEquals("Ny oppskrift", recipes.get(0).getName());
  }

  @Test
  /** Verifies that adding recipes preserves recipes already stored in the file. */
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
  /** Verifies that loading by name returns the matching recipe. */
  void loadShouldReturnRecipeWithMatchingName() throws IOException {
    Path path = tempDir.resolve("recipes.json");

    handler.saveAll(List.of(createRecipe("Pannekaker"), createRecipe("Pizza")), path);

    Recipe recipe = handler.load(path, "Pizza");

    assertEquals("Pizza", recipe.getName());
  }

  @Test
  /** Verifies that loading an unknown recipe name throws an exception. */
  void loadShouldThrowExceptionWhenRecipeDoesNotExist() throws IOException {
    Path path = tempDir.resolve("recipes.json");

    handler.saveAll(List.of(createRecipe("Pizza")), path);

    assertThrows(IllegalArgumentException.class, () -> handler.load(path, "Pannekaker"));
  }

  @Test
  /** Verifies that loading malformed JSON throws an I/O exception. */
  void loadAllShouldThrowExceptionForInvalidJson() throws IOException {
    Path path = createFile("recipes.json", "Dette er ikke gyldig JSON");

    assertThrows(IOException.class, () -> handler.loadAll(path));
  }

  @Test
  /** Verifies that an ingredient's amount and unit survive serialization. */
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
