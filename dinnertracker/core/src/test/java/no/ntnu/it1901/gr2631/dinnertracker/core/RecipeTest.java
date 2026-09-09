package no.ntnu.it1901.gr2631.dinnertracker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * Tests the functionality of the Recipe class.
 */
public class RecipeTest {
    private Recipe recipe;

    @BeforeEach 
    void setup() {
        recipe = new Recipe("Pannekaker");
    }

    /**
     * Tests if the correct name has returned. 
     */
    @Test 
    void newRecipeHasCorrectName() {
        assertEquals("Pannekaker", recipe.getName());
    }

    /**
     * Tests that a newly created recipe starts with empty
     * ingridient and step lists.
     */
    @Test 
    void newRecipeStartsWithEmptyList() {
        assertTrue(recipe.getIngridients().isEmpty());
        assertTrue(recipe.getSteps().isEmpty());
    }

    /**
     * Tests that the constructor which takes wxisting ingridients
     * and steps correctly sets the recipe's name and content.
     */
    @Test 
    void constructorWithExistingDataSetsCorrectContent() {
        List <FoodItem> ingridients = List.of(new FoodItem("Melk"));
        List<String> steps = List.of("Visp sammen");

        Recipe loaded = new Recipe("Lasagne",ingridients,steps);

        assertEquals("Lasagne", loaded.getName());
        assertEquals(1, loaded.getIngridients().size());
        assertEquals(1, loaded.getSteps().size());
    }

    /**
     * Tests that addIngridient() works, and adds exactly 
     * one ingridient to the recipe.
     */
    @Test 
    void addIngridientAddsOneIngridient() {
        FoodItem melk = new FoodItem("melk");
        recipe.addIngridient(melk);

        assertEquals(1, recipe.getIngridients().size());
        assertTrue(recipe.getIngridients().contains(melk));
    }

    /**
     * Tests that addStep() works, and adds steps
     * in the order they were added.
     */
    @Test 
    void addStepsAddsStepInCorrectOrder() {
        recipe.addStep("Visp sammen egg og melk");
        recipe.addStep("Stek pannekakene");

        assertEquals(2, recipe.getSteps().size());
        assertEquals("Visp sammen egg og melk", recipe.getSteps().get(0));
        assertEquals("Stek pannekakene", recipe.getSteps().get(1));
    }

    /**
     * Tests that remoceStep() removes the step at the given index,
     * leaving the remaining steps intact.
     */
    @Test 
    void removeStepRemovesCorrectStep() {
        recipe.addStep("Steg A");
        recipe.addStep("Steg B");

        recipe.removeStep(0);

        assertEquals(1, recipe.getSteps().size());
        assertEquals("Steg B", recipe.getSteps().get(0));
    }

    /**
     * Tests that removeIngridient() removes the specified ingridient,
     * leaving the other ingridients intact.
     */
    @Test
    void removingIngridientRemovesCorrectIngridient() {
        FoodItem melk = new FoodItem("Melk");
        FoodItem egg = new FoodItem("Egg");

        recipe.addIngridient(melk);
        recipe.addIngridient(egg);

        recipe.removeIngridient(melk);

        assertEquals(1, recipe.getIngridients().size());
        assertTrue(recipe.getIngridients().contains(egg));
        assertFalse(recipe.getIngridients().contains(melk));
    }

    /**
     * Tests that the list returned by getIngridients() is unmodifiable,
     * and throws an exception if modification is attempted.
     */
    @Test 
    void getIngridientsCannotBeModifiesDirectly() {
        assertThrows(UnsupportedOperationException.class,() -> {
            recipe.getIngridients().add(new FoodItem("Sukker"));
        });
    }

    /**
     * Tests that the list returned by getSteps() is unmodifiable,
     * and throws an exception if modification is attempted. 
     */
    @Test 
    void getStepsCannotBeModifiedDireclty() {
        assertThrows(UnsupportedOperationException.class, () -> {
            recipe.getSteps().add("Nytt steg");
        });
    }
}
