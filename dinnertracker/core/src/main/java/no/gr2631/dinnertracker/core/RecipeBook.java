package no.gr2631.dinnertracker.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Represents a collection of recipes associated with a user. */
public class RecipeBook {
  private final List<Recipe> recipes;
  private final User userName;

  /**
   * Creates an empty recipe book associated with the given user.
   *
   * @param userName the user associated with the recipe book
   */
  public RecipeBook(User userName) {
    this.recipes = new ArrayList<>();
    this.userName = userName;
  }

  /**
   * Adds a recipe to the end of the recipe book. Duplicate recipes are allowed.
   *
   * @param recipe the recipe to add
   * @throws IllegalArgumentException if the recipe is null
   */
  public void addRecipe(Recipe recipe) {
    if (recipe == null) {
      throw new IllegalArgumentException("Oppskriften kan ikke være null.");
    }
    recipes.add(recipe);
  }

  /**
   * Uses a recipe by removing the required amount of each ingredient from the fridge. Each
   * ingredient in the recipe must exist in the fridge in a sufficient amount and with the same
   * unit. If an ingredient cannot be used, ingredients already removed are not restored.
   *
   * @param recipe the recipe whose ingredients should be used
   * @param fridge the fridge to remove the ingredients from
   * @throws IllegalArgumentException if an ingredient is missing, has an incompatible unit, or is
   *     available in an insufficient amount
   * @throws NullPointerException if the recipe is null, or the fridge is null and the recipe has
   *     ingredients
   */
  public void useRecipe(Recipe recipe, Fridge fridge) {
    for (Map.Entry<FoodItem, Amount> entry : recipe.getIngredients().entrySet()) {
      fridge.useFoodItem(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Gets an unmodifiable view of the recipes in insertion order. The view reflects later additions
   * to the recipe book, and the recipes themselves can still be modified.
   *
   * @return an unmodifiable view of the recipes
   */
  public List<Recipe> getRecipes() {
    return Collections.unmodifiableList(recipes);
  }

  /**
   * Finds the first recipe with the given name using a case-sensitive comparison.
   *
   * @param name the recipe name to search for
   * @return the first recipe with the given name
   * @throws IllegalArgumentException if no recipe has the given name, including when the name is
   *     null
   */
  public Recipe getRecipeByName(String name) {
    for (Recipe recipe : recipes) {
      if (recipe.getName().equals(name)) {
        return recipe;
      }
    }
    throw new IllegalArgumentException("Ingen oppskrift med navnet " + name + " finnes.");
  }

  /**
   * Gets the user associated with this recipe book.
   *
   * @return the associated user, or null if no user was supplied
   */
  public User getUserName() {
    return userName;
  }
}
