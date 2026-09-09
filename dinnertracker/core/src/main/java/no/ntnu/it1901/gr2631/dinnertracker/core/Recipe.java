package no.ntnu.it1901.gr2631.dinnertracker.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** 
 * Represents a recipe in our dinner tracker.
*/
public class Recipe {
    private final String name;
    private final List<FoodItem> ingridients;
    private final List<String> steps;

    /** 
     * Creates a new recipe with existing ingredients and steps.
     * Used when loading an already complete recipe, for example from a file
     * or a web source (this constructor may need to change to fit file reading.)
     * 
     * @param name the name of the recipe
     * @param ingridients the list of food items required for the recipe
     * @param steps the list of steps describing how to prepare the recipe
    */
    public Recipe(String name,List<FoodItem> ingridients, List<String> steps) {
        this.name = name;
        this.ingridients = new ArrayList<>(ingridients);
        this.steps = new ArrayList<>(steps);
    }

    /**
     * Creates a new, empty recipe with the given name.
     * Used when a user wants to build up a recipe themselves, by
     * adding ingredients and steps afterwards. 
     * @param name
     * @throws IllegalArgumentException if the name is null or blank.
     */
    public Recipe(String name) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("Navnet kan ikke være tomt.");
        }
        this.name = name;
        this.ingridients = new ArrayList<>();
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
     * Gets the list of ingridients required for the recipe.
     * The returned list is unmodifiable. Need to use addIngridient()
     * to add ingridients to the recipe.
     * 
     * @return an unmodifiable list of the recipe's ingridients
     */
    public List<FoodItem> getIngridients() {
        return Collections.unmodifiableList(ingridients);
        
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
     * Adds an ingridients to the recipe.
     * 
     * @param item the food item to add as an ingridient
     */
    public void addIngridient(FoodItem item) {
        ingridients.add(item);
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
     * Removes the ingridient at the given index from the recipe.
     * 
     * @param item the item to remove from ingridients
     */
    public void removeIngridient(FoodItem item) {
        ingridients.remove(item);
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
     * Returns a formatted representation of the recipe,
     * including its name, ingredients, and numbered steps.
     * 
     * @return a formatted string representation of the recipe
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Oppskrift: ").append(name).append("\n\n");

        sb.append("Ingridienser:\n");
        for(FoodItem item:ingridients) {
            sb.append("- ").append(item).append("\n");
        }

        sb.append("\nFremgangsmåte:\n");
        for(int i = 0; i<steps.size(); i++) {
            sb.append(i+1).append(".").append(steps.get(i)).append("\n");
        }

        return sb.toString();
    }
}
