package no.gr2631.dinnertracker.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests reading and writing recipes with {@link RecipeFileHandler}. */
class RecipeFileHandlerTest {

  private RecipeFileHandler handler;
  private User user;

  @TempDir Path tempDir;

  /** Creates a fresh file handler before each test. */
  @BeforeEach
  void setUp() {
    handler = new RecipeFileHandler();
    user = new User("testuser");
  }

  /** Creates a file in the temporary test directory with the given content. */
  private Path createFile(String fileName, String content) throws IOException {
    Path path = tempDir.resolve(fileName);
    Files.writeString(path, content);
    return path;
  }

  /** Creates a recipe book containing the given recipes. */
  private RecipeBook createRecipeBook(Recipe... recipes) {
    RecipeBook book = new RecipeBook(user);
    for (Recipe recipe : recipes) {
      book.addRecipe(recipe);
    }
    return book;
  }

  /** Creates a recipe containing one ingredient and two preparation steps. */
  private Recipe createRecipe(String name) {
    Recipe recipe = new Recipe(name, user);
    recipe.addIngredient(new FoodItem("Mel"), new Amount(500, Unit.GRAM));
    recipe.addStep("Bland ingrediensene");
    recipe.addStep("Stek i ovnen");

    return recipe;
  }

  /** Verifies that loading a missing file returns an empty recipe book. */
  @Test
  void loadShouldReturnEmptyRecipeBookWhenFileDoesNotExist() throws IOException {
    Path path = tempDir.resolve("recipes.json");

    List<Recipe> recipes = handler.load(path, user).getRecipes();

    assertTrue(recipes.isEmpty());
  }

  /** Verifies that loading an empty file returns an empty recipe book. */
  @Test
  void loadShouldReturnEmptyRecipeBookWhenFileIsEmpty() throws IOException {
    Path path = createFile("recipes.json", "");

    List<Recipe> recipes = handler.load(path, user).getRecipes();

    assertTrue(recipes.isEmpty());
  }

  /** Verifies that saving and loading preserves the recipe data. */
  @Test
  void saveAndLoadShouldPreserveRecipe() throws IOException {
    Path path = tempDir.resolve("recipes.json");
    Recipe recipe = createRecipe("Pannekaker");

    handler.save(path, createRecipeBook(recipe));

    List<Recipe> recipes = handler.load(path, user).getRecipes();

    assertEquals(1, recipes.size());
    assertEquals("Pannekaker", recipes.get(0).getName());
    assertEquals(recipe.getSteps(), recipes.get(0).getSteps());
    assertEquals(1, recipes.get(0).getIngredients().size());
  }

  /** Verifies that saving recipes replaces existing file contents. */
  @Test
  void saveShouldOverwriteExistingRecipes() throws IOException {
    Path path = tempDir.resolve("recipes.json");

    handler.save(path, createRecipeBook(createRecipe("Gammel oppskrift")));
    handler.save(path, createRecipeBook(createRecipe("Ny oppskrift")));

    List<Recipe> recipes = handler.load(path, user).getRecipes();

    assertEquals(1, recipes.size());
    assertEquals("Ny oppskrift", recipes.get(0).getName());
  }

  /** Verifies that adding recipes preserves recipes already stored in the file. */
  @Test
  void addRecipeShouldKeepExistingRecipes() throws IOException {
    Path path = tempDir.resolve("recipes.json");

    handler.save(path, createRecipeBook(createRecipe("Pannekaker")));
    RecipeBook book = handler.load(path, user);
    book.addRecipe(createRecipe("Pizza"));
    handler.save(path, book);

    List<Recipe> recipes = handler.load(path, user).getRecipes();

    assertEquals(2, recipes.size());
    assertEquals("Pannekaker", recipes.get(0).getName());
    assertEquals("Pizza", recipes.get(1).getName());
  }

  /** Verifies that loading by name returns the matching recipe. */
  @Test
  void loadShouldReturnRecipeWithMatchingName() throws IOException {
    Path path = tempDir.resolve("recipes.json");

    handler.save(path, createRecipeBook(createRecipe("Pannekaker"), createRecipe("Pizza")));

    Recipe recipe = handler.load(path, user).getRecipeByName("Pizza");

    assertEquals("Pizza", recipe.getName());
  }

  /** Verifies that loading an unknown recipe name throws an exception. */
  @Test
  void loadShouldThrowExceptionWhenRecipeDoesNotExist() throws IOException {
    Path path = tempDir.resolve("recipes.json");

    handler.save(path, createRecipeBook(createRecipe("Pizza")));

    assertThrows(
        IllegalArgumentException.class,
        () -> handler.load(path, user).getRecipeByName("Pannekaker"));
  }

  /** Verifies that loading malformed JSON throws an I/O exception. */
  @Test
  void loadShouldThrowExceptionForInvalidJson() throws IOException {
    Path path = createFile("recipes.json", "Dette er ikke gyldig JSON");

    assertThrows(IOException.class, () -> handler.load(path, user));
  }

  @ParameterizedTest
  @ValueSource(strings = {"null", "[]", "[null]"})
  void loadShouldHandleEmptyJsonData(String json) throws IOException {
    Path path = createFile("recipes.json", json);

    assertTrue(handler.load(path, user).getRecipes().isEmpty());
  }

  @Test
  void saveShouldAllowClearingRecipeBook() throws IOException {
    Path path = tempDir.resolve("recipes.json");
    handler.save(path, createRecipeBook(createRecipe("Pizza")));

    handler.save(path, new RecipeBook(user));

    assertTrue(handler.load(path, user).getRecipes().isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {"GRAM", "gram", "g"})
  void loadShouldAcceptUnitNamesAndSymbols(String unit) throws IOException {
    Path path =
        createFile(
            "recipes.json",
            """
        [{"userName":"testuser","name":"Bread","ingredients":[null,
          {"name":"Mel","quantity":500.5,"unit":"%s"}],"steps":[null,"Bake"]}]
        """
                .formatted(unit));

    Recipe recipe = handler.load(path, user).getRecipes().get(0);

    assertEquals(new Amount(500.5, Unit.GRAM), recipe.getIngredients().get(new FoodItem("Mel")));
    assertEquals(List.of("Bake"), recipe.getSteps());
  }

  /** Verifies that an ingredient's amount and unit survive serialization. */
  @Test
  void loadShouldPreserveIngredientAmountAndUnit() throws IOException {
    Path path = tempDir.resolve("recipes.json");

    Recipe recipe = createRecipe("Pannekaker");
    handler.save(path, createRecipeBook(recipe));

    Recipe loaded = handler.load(path, user).getRecipes().get(0);

    FoodItem flour = loaded.getIngredients().keySet().iterator().next();
    Amount amount = loaded.getIngredients().get(flour);

    assertEquals("Mel", flour.getName());
    assertEquals(500, amount.quantity());
    assertEquals(Unit.GRAM, amount.unit());
  }
}
