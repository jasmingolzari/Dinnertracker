package no.gr2631.dinnertracker.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests recipe storage, lookup, collection access, and ingredient consumption. */
public class RecipeBookTest {

  private User user;
  private RecipeBook recipeBook;
  private Fridge fridge;

  /** Creates a fresh user, recipe book, and fridge for each test. */
  @BeforeEach
  void setUp() {
    user = new User("testuser");
    recipeBook = new RecipeBook(user);
    fridge = new Fridge(user);
  }

  /** A new book belongs to its supplied user and contains no recipes. */
  @Test
  void newRecipeBookShouldBeEmptyAndRetainUser() {
    assertTrue(recipeBook.getRecipes().isEmpty());
    assertSame(user, recipeBook.getUserName());
  }

  /** An absent owner is retained without inventing a default user. */
  @Test
  void constructorShouldAllowNullUser() {
    RecipeBook book = new RecipeBook(null);

    assertNull(book.getUserName());
    assertTrue(book.getRecipes().isEmpty());
  }

  /** Recipes are returned in the order they were added. */
  @Test
  void addRecipeShouldPreserveInsertionOrder() {
    Recipe pasta = new Recipe("Pasta", user);
    Recipe soup = new Recipe("Soup", user);

    recipeBook.addRecipe(pasta);
    recipeBook.addRecipe(soup);

    assertEquals(List.of(pasta, soup), recipeBook.getRecipes());
  }

  /** Adding the same recipe more than once is allowed. */
  @Test
  void addRecipeShouldAllowDuplicates() {
    Recipe pasta = new Recipe("Pasta", user);

    recipeBook.addRecipe(pasta);
    recipeBook.addRecipe(pasta);

    assertEquals(List.of(pasta, pasta), recipeBook.getRecipes());
  }

  /** Rejecting a null recipe leaves existing entries intact. */
  @Test
  void addRecipeShouldRejectNull() {
    Recipe pasta = new Recipe("Pasta", user);
    recipeBook.addRecipe(pasta);

    assertThrows(IllegalArgumentException.class, () -> recipeBook.addRecipe(null));

    assertEquals(List.of(pasta), recipeBook.getRecipes());
  }

  /** Callers cannot add, remove, or clear recipes through the returned list. */
  @Test
  void getRecipesShouldReturnUnmodifiableList() {
    Recipe pasta = new Recipe("Pasta", user);
    recipeBook.addRecipe(pasta);
    List<Recipe> recipes = recipeBook.getRecipes();

    assertAll(
        () -> assertThrows(UnsupportedOperationException.class, () -> recipes.add(pasta)),
        () -> assertThrows(UnsupportedOperationException.class, () -> recipes.remove(pasta)),
        () -> assertThrows(UnsupportedOperationException.class, recipes::clear));

    assertEquals(List.of(pasta), recipeBook.getRecipes());
  }

  /** A previously obtained view reflects recipes added later. */
  @Test
  void getRecipesShouldReflectLaterAdditions() {
    List<Recipe> recipes = recipeBook.getRecipes();
    Recipe pasta = new Recipe("Pasta", user);

    recipeBook.addRecipe(pasta);

    assertEquals(List.of(pasta), recipes);
  }

  /** Name lookup returns the actual matching recipe. */
  @Test
  void getRecipeByNameShouldReturnMatchingRecipe() {
    Recipe pasta = new Recipe("Pasta", user);
    Recipe soup = new Recipe("Soup", user);
    recipeBook.addRecipe(pasta);
    recipeBook.addRecipe(soup);

    assertSame(soup, recipeBook.getRecipeByName("Soup"));
  }

  /** Recipes with the same name are allowed, and lookup returns the first one. */
  @Test
  void getRecipeByNameShouldReturnFirstMatchingRecipe() {
    Recipe first = new Recipe("Pasta", user);
    Recipe second = new Recipe("Pasta", user);
    recipeBook.addRecipe(first);
    recipeBook.addRecipe(second);

    assertSame(first, recipeBook.getRecipeByName("Pasta"));
    assertEquals(List.of(first, second), recipeBook.getRecipes());
  }

  /** Lookup requires an exact name and rejects missing or null names. */
  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "Missing", "pasta", " Pasta "})
  void getRecipeByNameShouldRejectUnmatchedName(String name) {
    recipeBook.addRecipe(new Recipe("Pasta", user));

    assertThrows(IllegalArgumentException.class, () -> recipeBook.getRecipeByName(name));
  }

  /** Looking up a recipe in an empty book fails. */
  @Test
  void getRecipeByNameShouldRejectLookupInEmptyBook() {
    assertThrows(IllegalArgumentException.class, () -> recipeBook.getRecipeByName("Pasta"));
  }

  /** Using a recipe consumes every required amount and leaves unrelated food unchanged. */
  @Test
  void useRecipeShouldConsumeIngredientsAndPreserveRecipe() {
    FoodItem milk = new FoodItem("Milk");
    FoodItem eggs = new FoodItem("Eggs");
    final FoodItem flour = new FoodItem("Flour");
    Recipe recipe = new Recipe("Pancakes", user);
    recipe.addIngredient(milk, new Amount(0.25, Unit.LITRE));
    recipe.addIngredient(eggs, new Amount(1.25, Unit.PIECE));
    recipe.addStep("Mix the ingredients");
    recipeBook.addRecipe(recipe);
    final Map<FoodItem, Amount> ingredients = Map.copyOf(recipe.getIngredients());
    fridge.addFoodItem(milk, new Amount(1.5, Unit.LITRE));
    fridge.addFoodItem(eggs, new Amount(5.5, Unit.PIECE));
    fridge.addFoodItem(flour, new Amount(500, Unit.GRAM));

    recipeBook.useRecipe(recipe, fridge);

    assertEquals(
        Map.of(
            milk, new Amount(1.25, Unit.LITRE),
            eggs, new Amount(4.25, Unit.PIECE),
            flour, new Amount(500, Unit.GRAM)),
        fridge.getFoodItems());
    assertEquals(ingredients, recipe.getIngredients());
    assertEquals(List.of("Mix the ingredients"), recipe.getSteps());
    assertEquals(List.of(recipe), recipeBook.getRecipes());
  }

  /** Fully consumed ingredients are removed from the fridge. */
  @Test
  void useRecipeShouldRemoveFullyConsumedIngredients() {
    FoodItem milk = new FoodItem("Milk");
    FoodItem eggs = new FoodItem("Eggs");
    Recipe recipe = new Recipe("Pancakes", user);
    recipe.addIngredient(milk, new Amount(0.5, Unit.LITRE));
    recipe.addIngredient(eggs, new Amount(2, Unit.PIECE));
    recipeBook.addRecipe(recipe);
    fridge.addFoodItem(milk, new Amount(0.5, Unit.LITRE));
    fridge.addFoodItem(eggs, new Amount(2, Unit.PIECE));

    recipeBook.useRecipe(recipe, fridge);

    assertTrue(fridge.getFoodItems().isEmpty());
    assertSame(recipe, recipeBook.getRecipeByName("Pancakes"));
  }

  /** A missing ingredient prevents its use and leaves unrelated stock unchanged. */
  @Test
  void useRecipeShouldRejectMissingIngredient() {
    Recipe recipe = new Recipe("Pancakes", user);
    recipe.addIngredient(new FoodItem("Milk"), new Amount(0.5, Unit.LITRE));
    fridge.addFoodItem(new FoodItem("Flour"), new Amount(500, Unit.GRAM));
    Map<FoodItem, Amount> originalStock = fridge.getFoodItems();

    assertThrows(IllegalArgumentException.class, () -> recipeBook.useRecipe(recipe, fridge));

    assertEquals(originalStock, fridge.getFoodItems());
  }

  /** Insufficient stock is rejected without changing the available quantity. */
  @Test
  void useRecipeShouldRejectInsufficientQuantity() {
    FoodItem milk = new FoodItem("Milk");
    Recipe recipe = new Recipe("Pancakes", user);
    recipe.addIngredient(milk, new Amount(1.0, Unit.LITRE));
    fridge.addFoodItem(milk, new Amount(0.5, Unit.LITRE));
    Map<FoodItem, Amount> originalStock = fridge.getFoodItems();

    assertThrows(IllegalArgumentException.class, () -> recipeBook.useRecipe(recipe, fridge));

    assertEquals(originalStock, fridge.getFoodItems());
  }

  /** A matching ingredient with an incompatible unit cannot be consumed. */
  @Test
  void useRecipeShouldRejectDifferentUnit() {
    FoodItem milk = new FoodItem("Milk");
    Recipe recipe = new Recipe("Pancakes", user);
    recipe.addIngredient(milk, new Amount(0.5, Unit.LITRE));
    fridge.addFoodItem(milk, new Amount(500, Unit.GRAM));
    Map<FoodItem, Amount> originalStock = fridge.getFoodItems();

    assertThrows(IllegalArgumentException.class, () -> recipeBook.useRecipe(recipe, fridge));

    assertEquals(originalStock, fridge.getFoodItems());
  }

  /** An empty recipe leaves the fridge unchanged. */
  @Test
  void useRecipeShouldLeaveFridgeUnchangedForEmptyRecipe() {
    Recipe recipe = new Recipe("Empty", user);
    fridge.addFoodItem(new FoodItem("Milk"), new Amount(0.5, Unit.LITRE));
    Map<FoodItem, Amount> originalStock = fridge.getFoodItems();

    recipeBook.useRecipe(recipe, fridge);

    assertEquals(originalStock, fridge.getFoodItems());
  }

  /** A null recipe cannot be used. */
  @Test
  void useRecipeShouldRejectNullRecipe() {
    assertThrows(NullPointerException.class, () -> recipeBook.useRecipe(null, fridge));
  }

  /** A recipe containing ingredients requires a fridge. */
  @Test
  void useRecipeShouldRejectNullFridgeForRecipeWithIngredients() {
    Recipe recipe = new Recipe("Pancakes", user);
    recipe.addIngredient(new FoodItem("Milk"), new Amount(0.5, Unit.LITRE));

    assertThrows(NullPointerException.class, () -> recipeBook.useRecipe(recipe, null));
  }
}
