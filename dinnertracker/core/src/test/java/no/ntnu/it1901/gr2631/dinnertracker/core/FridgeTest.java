package no.ntnu.it1901.gr2631.dinnertracker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests adding, using, and querying fractional food quantities in a fridge.
 */
class FridgeTest {

    private static final double DELTA = 0.000001;

    /**
     * Verifies that a food item can be added with a fractional quantity.
     */
    @Test
    void testAddFoodItem() {
        FoodItem milk = new FoodItem("Milk");

        Fridge.addFoodItem(milk, 5.5);

        assertEquals(5.5, Fridge.getQuantity(milk), DELTA);
    }

    /**
     * Verifies that adding an existing food item increases its quantity.
     */
    @Test
    void testAddExistingFoodItemAddsQuantity() {
        FoodItem eggs = new FoodItem("Eggs");

        Fridge.addFoodItem(eggs, 4.25);
        Fridge.addFoodItem(eggs, 3.75);

        assertEquals(8.0, Fridge.getQuantity(eggs), DELTA);
    }

    /**
     * Verifies that querying an item not in the fridge returns zero.
     */
    @Test
    void testGetQuantityOfNonExistingFoodItem() {
        FoodItem cheese = new FoodItem("Cheese");

        assertEquals(0.0, Fridge.getQuantity(cheese), DELTA);
    }

    /**
     * Verifies that using part of an item's quantity succeeds and updates the remainder.
     */
    @Test
    void testUseFoodItem() {
        FoodItem bread = new FoodItem("Bread");

        Fridge.addFoodItem(bread, 5.5);

        boolean result = Fridge.useFoodItem(bread, 2.25);

        assertTrue(result);
        assertEquals(3.25, Fridge.getQuantity(bread), DELTA);
    }

    /**
     * Verifies that using an item's exact quantity removes it from the fridge.
     */
    @Test
    void testUseExactQuantityRemovesFoodItem() {
        FoodItem butter = new FoodItem("Butter");

        Fridge.addFoodItem(butter, 5.5);

        boolean result = Fridge.useFoodItem(butter, 5.5);

        assertTrue(result);
        assertEquals(0.0, Fridge.getQuantity(butter), DELTA);
    }

    /**
     * Verifies that using more than the available quantity fails without changing it.
     */
    @Test
    void testCannotUseMoreThanAvailableQuantity() {
        FoodItem yogurt = new FoodItem("Yogurt");

        Fridge.addFoodItem(yogurt, 3.5);

        boolean result = Fridge.useFoodItem(yogurt, 5.25);

        assertFalse(result);

        assertEquals(3.5, Fridge.getQuantity(yogurt), DELTA);
    }

    /**
     * Verifies that using an item that is not in the fridge fails.
     */
    @Test
    void testCannotUseFoodItemThatDoesNotExist() {
        FoodItem chicken = new FoodItem("Chicken");

        boolean result = Fridge.useFoodItem(chicken, 1.25);

        assertFalse(result);
        assertEquals(0.0, Fridge.getQuantity(chicken), DELTA);
    }

    /**
     * Verifies that adding a negative quantity throws an exception.
     */
    @Test
    void testAddNegativeQuantityThrowsException() {
        FoodItem apples = new FoodItem("Apples");

        assertThrows(
            IllegalArgumentException.class,
            () -> Fridge.addFoodItem(apples, -1.0)
        );
    }

    /**
     * Verifies that using a negative quantity throws an exception and preserves stock.
     */
    @Test
    void testUseNegativeQuantityThrowsException() {
        FoodItem bananas = new FoodItem("Bananas");

        Fridge.addFoodItem(bananas, 5.5);

        assertThrows(
            IllegalArgumentException.class,
            () -> Fridge.useFoodItem(bananas, -1.0)
        );

        assertEquals(5.5, Fridge.getQuantity(bananas), DELTA);
    }
}