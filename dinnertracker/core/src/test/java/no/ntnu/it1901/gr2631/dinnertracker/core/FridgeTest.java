package no.ntnu.it1901.gr2631.dinnertracker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FridgeTest {

    @Test
    void testAddFoodItem() {
        FoodItem milk = new FoodItem("Milk");

        Fridge.addFoodItem(milk, 5);

        assertEquals(5, Fridge.getQuantity(milk));
    }

    @Test
    void testAddExistingFoodItemAddsQuantity() {
        FoodItem eggs = new FoodItem("Eggs");

        Fridge.addFoodItem(eggs, 4);
        Fridge.addFoodItem(eggs, 3);

        assertEquals(7, Fridge.getQuantity(eggs));
    }

    @Test
    void testGetQuantityOfNonExistingFoodItem() {
        FoodItem cheese = new FoodItem("Cheese");

        assertEquals(0, Fridge.getQuantity(cheese));
    }

    @Test
    void testUseFoodItem() {
        FoodItem bread = new FoodItem("Bread");

        Fridge.addFoodItem(bread, 5);

        boolean result = Fridge.useFoodItem(bread, 2);

        assertTrue(result);
        assertEquals(3, Fridge.getQuantity(bread));
    }

    @Test
    void testUseExactQuantityRemovesFoodItem() {
        FoodItem butter = new FoodItem("Butter");

        Fridge.addFoodItem(butter, 5);

        boolean result = Fridge.useFoodItem(butter, 5);

        assertTrue(result);
        assertEquals(0, Fridge.getQuantity(butter));
    }

    @Test
    void testCannotUseMoreThanAvailableQuantity() {
        FoodItem yogurt = new FoodItem("Yogurt");

        Fridge.addFoodItem(yogurt, 3);

        boolean result = Fridge.useFoodItem(yogurt, 5);

        assertFalse(result);

        assertEquals(3, Fridge.getQuantity(yogurt));
    }

    @Test
    void testCannotUseFoodItemThatDoesNotExist() {
        FoodItem chicken = new FoodItem("Chicken");

        boolean result = Fridge.useFoodItem(chicken, 1);

        assertFalse(result);
        assertEquals(0, Fridge.getQuantity(chicken));
    }

    @Test
    void testAddNegativeQuantityThrowsException() {
        FoodItem apples = new FoodItem("Apples");

        assertThrows(
            IllegalArgumentException.class,
            () -> Fridge.addFoodItem(apples, -1)
        );
    }

    @Test
    void testUseNegativeQuantityThrowsException() {
        FoodItem bananas = new FoodItem("Bananas");

        Fridge.addFoodItem(bananas, 5);

        assertThrows(
            IllegalArgumentException.class,
            () -> Fridge.useFoodItem(bananas, -1)
        );

        assertEquals(5, Fridge.getQuantity(bananas));
    }
}