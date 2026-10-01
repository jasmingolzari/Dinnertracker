package no.ntnu.it1901.gr2631.dinnertracker.core;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/** 
 * Represents a recipe in our dinner tracker.
*/
public final class Recipe {
    private String name;
    private Map<FoodItem,Amount> ingredients;
    private List<String> steps;

    /** 
     * Creates a new recipe with existing ingredients and steps.
     * Used when loading a complete recipe from file.
     * 
     * @param name the name of the recipe
     * @param ingredients map from food item to the amount required
     * @param steps the list of steps describing how to prepare the recipe
    */
    public Recipe(String name,Map<FoodItem,Amount> ingredients, List<String> steps) {
        this(name);
        ingredients.forEach(this::addIngredient);
        this.steps.addAll(steps);

    }

    /**
     * Creates a new, empty recipe with the given name.
     * Used when a user wants to build up a recipe themselves, by
     * adding ingredients and steps afterwards. 
     * 
     * @param name
     * @throws IllegalArgumentException if the name is null or blank.
     */
    public Recipe(String name) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("Navnet kan ikke være tomt.");
        }
        this.name = name;
        this.ingredients = new HashMap<>();
        this.steps = new ArrayList<>();
    }

    /**
     * Gets the name of the recipe.
     * 
     * @return the name of the recipe
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the list of ingredients required for the recipe.
     * The returned list is unmodifiable. Need to use addIngredient()
     * to add ingredients to the recipe.
     * 
     * @return an unmodifiable list of the recipe's ingredients
     */
    public Map<FoodItem, Amount> getIngredients() {
        return Collections.unmodifiableMap(ingredients);
    }
    
    /**
     * Gets the list of steps describing how to prepare the recipe.
     * The returned list is unmodifiable. Need to use addStep()
     * to add steps to the recipe
     * 
     * @return an unmodifiable list of the recipe's steps, in order
     */
    public List<String> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * Adds an ingredient to the recipe.
     * 
     * @param item the food item to add as an ingredient
     */
    public void addIngredient(FoodItem item, Amount amount) {
        if (item == null) {
            throw new IllegalArgumentException("Matvare kan ikke være null.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Mengde kan ikke være null.");
        }
        ingredients.put(item, amount);
    }

    /**
     * Adds a step to the end of the recipe's list of steps.
     * 
     * @param step the step to add
     */
    public void addStep(String step) {
        steps.add(step);
    }

    /**
     * Removes the ingredient at the given index from the recipe.
     * 
     * @param item the item to remove from ingredients
     */
    public void removeIngredient(FoodItem item) {
        ingredients.remove(item);
    }

    /**
     * Removes the step at the given index from the recipe.
     * 
     * @param index the index of the step to remove
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public void removeStep(int index) {
        steps.remove(index);
    }

    /**
     * Uses a recipe by removing the required amount of each ingredient from the fridge.
     * Each ingredient in the recipe must exist in the fridge in a sufficient amount.
     *
     * @param recipe the recipe whose ingredients should be used
     * @param fridge the fridge to remove the ingredients from
     * @throws IllegalArgumentException if the fridge does not contain enough of an ingredient
     */
    public void useRecipe(Recipe recipe, Fridge fridge) {
        for (Map.Entry<FoodItem, Amount> entry : recipe.getIngredients().entrySet()) {
            fridge.useFoodItem(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns a formatted representation of the recipe,
     * including its name, ingredients, and numbered steps.
     * 
     * @return a formatted string representation of the recipe
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Oppskrift: ").append(name).append("\n\n");

        sb.append("Ingredienser:\n");
        for (Map.Entry<FoodItem, Amount> entry : ingredients.entrySet()) {
            Amount a = entry.getValue();
            sb.append("- ").append(entry.getKey().getName()).append(": ")
              .append(a.quantity()).append(" ")
              .append(a.unit().getSymbol()).append("\n");
        }

        sb.append("\nFremgangsmåte:\n");
        for (int i = 0; i < steps.size(); i++) {
            sb.append(i + 1).append(". ").append(steps.get(i)).append("\n");
        }

        return sb.toString();
    }
}
