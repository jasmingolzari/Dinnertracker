package no.gr2631.dinnertracker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests the functionality of the Recipe class. */
public class RecipeTest {
  private Recipe recipe;
  private Amount melkAmount;
  private Amount eggAmount;
  private User userName;

  @BeforeEach
  void setup() {
    userName = new User("testuser");
    recipe = new Recipe("Pannekaker", userName);

    melkAmount = new Amount(0.5, Unit.LITRE);
    eggAmount = new Amount(3, Unit.PIECE);
  }

  /** Tests if the correct name has returned. */
  @Test
  void newRecipeHasCorrectName() {
    assertEquals("Pannekaker", recipe.getName());
  }

  /** Tests that the constructor throws if the name is null. */
  @Test
  void constructorThrowsOnNullName() {
    assertThrows(IllegalArgumentException.class, () -> new Recipe(null, userName));
  }

  /** Tests that the constructor throws if the name is blank. */
  @Test
  void constructorThrowsOnBlankName() {
    assertThrows(IllegalArgumentException.class, () -> new Recipe("   ", userName));
  }

  /** Tests that a newly created recipe starts with empty ingredient and step collections. */
  @Test
  void newRecipeStartsWithEmptyCollections() {
    assertTrue(recipe.getIngredients().isEmpty());
    assertTrue(recipe.getSteps().isEmpty());
  }

  /**
   * Tests that the constructor which takes existing ingredients and steps correctly sets the
   * recipe's name and content.
   */
  @Test
  void constructorWithExistingDataSetsCorrectContent() {
    FoodItem melk = new FoodItem("Melk");
    Map<FoodItem, Amount> ingredients = Map.of(melk, melkAmount);
    List<String> steps = List.of("Visp sammen");

    Recipe loaded = new Recipe("Lasagne", userName, ingredients, steps);

    assertEquals("Lasagne", loaded.getName());
    assertEquals(1, loaded.getIngredients().size());
    assertEquals(melkAmount, loaded.getIngredients().get(melk));
    assertEquals(1, loaded.getSteps().size());
    assertEquals("Visp sammen", loaded.getSteps().get(0));
  }

  /** Tests that addIngredient() adds exactly one ingredient with the correct amount. */
  @Test
  void addIngredientAddsOneIngredientWithAmount() {
    FoodItem melk = new FoodItem("Melk");
    recipe.addIngredient(melk, melkAmount);

    assertEquals(1, recipe.getIngredients().size());
    assertTrue(recipe.getIngredients().containsKey(melk));
    assertEquals(melkAmount, recipe.getIngredients().get(melk));
  }

  /**
   * Tests that adding the same food item twice replaces the amount instead of creating a duplicate
   * entry.
   */
  @Test
  void addIngredientTwiceReplacesAmount() {
    FoodItem melk = new FoodItem("Melk");
    Amount newAmount = new Amount(1, Unit.LITRE);

    recipe.addIngredient(melk, melkAmount);
    recipe.addIngredient(melk, newAmount);

    assertEquals(1, recipe.getIngredients().size());
    assertEquals(newAmount, recipe.getIngredients().get(melk));
  }

  /** Tests that addIngredient() throws if the food item is null. */
  @Test
  void addIngredientThrowsOnNullItem() {
    assertThrows(IllegalArgumentException.class, () -> recipe.addIngredient(null, melkAmount));
  }

  /** Tests that addIngredient() throws if the amount is null. */
  @Test
  void addIngredientThrowsOnNullAmount() {
    FoodItem melk = new FoodItem("Melk");
    assertThrows(IllegalArgumentException.class, () -> recipe.addIngredient(melk, null));
  }

  /** Tests that addStep() adds steps in the order they were added. */
  @Test
  void addStepAddsStepInCorrectOrder() {
    recipe.addStep("Visp sammen egg og melk");
    recipe.addStep("Stek pannekakene");

    assertEquals(2, recipe.getSteps().size());
    assertEquals("Visp sammen egg og melk", recipe.getSteps().get(0));
    assertEquals("Stek pannekakene", recipe.getSteps().get(1));
  }

  /**
   * Tests that removeStep() removes the step at the given index, leaving the remaining steps
   * intact.
   */
  @Test
  void removeStepRemovesCorrectStep() {
    recipe.addStep("Steg A");
    recipe.addStep("Steg B");

    recipe.removeStep(0);

    assertEquals(1, recipe.getSteps().size());
    assertEquals("Steg B", recipe.getSteps().get(0));
  }

  /** Tests that removeStep() throws if the index is out of range. */
  @Test
  void removeStepThrowsOnInvalidIndex() {
    assertThrows(IndexOutOfBoundsException.class, () -> recipe.removeStep(0));
  }

  /**
   * Tests that removeIngredient() removes the specified ingredient, leaving the other ingredients
   * intact.
   */
  @Test
  void removeIngredientRemovesCorrectIngredient() {
    FoodItem melk = new FoodItem("Melk");
    FoodItem egg = new FoodItem("Egg");

    recipe.addIngredient(melk, melkAmount);
    recipe.addIngredient(egg, eggAmount);

    recipe.removeIngredient(melk);

    assertEquals(1, recipe.getIngredients().size());
    assertTrue(recipe.getIngredients().containsKey(egg));
    assertFalse(recipe.getIngredients().containsKey(melk));
  }

  /**
   * Tests that the map returned by getIngredients() is unmodifiable, and throws an exception if
   * modification is attempted.
   */
  @Test
  void getIngredientsCannotBeModifiedDirectly() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          recipe.getIngredients().put(new FoodItem("Sukker"), melkAmount);
        });
  }

  /**
   * Tests that the list returned by getSteps() is unmodifiable, and throws an exception if
   * modification is attempted.
   */
  @Test
  void getStepsCannotBeModifiedDirectly() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          recipe.getSteps().add("Nytt steg");
        });
  }

  /** Tests that toString() contains the name, ingredients and numbered steps. */
  @Test
  void toStringContainsNameIngredientsAndSteps() {
    recipe.addIngredient(new FoodItem("Melk"), melkAmount);
    recipe.addStep("Visp sammen");

    String result = recipe.toString();

    assertTrue(result.contains("Oppskrift: Pannekaker"));
    assertTrue(result.contains("Melk"));
    assertTrue(result.contains("1. Visp sammen"));
  }
}
