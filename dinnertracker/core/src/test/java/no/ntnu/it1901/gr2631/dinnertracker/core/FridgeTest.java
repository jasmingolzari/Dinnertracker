package no.ntnu.it1901.gr2631.dinnertracker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

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
        Fridge fridge = new Fridge();
        FoodItem milk = new FoodItem("Milk");

        fridge.addFoodItem(milk, 5.5, Unit.LITRE);

        assertEquals(5.5, fridge.getQuantity(milk), DELTA);
        assertEquals(Unit.LITRE, fridge.getUnit(milk));
    }

    /**
     * Verifies that adding an existing food item increases its quantity.
     */
    @Test
    void testAddExistingFoodItemAddsQuantity() {
        Fridge fridge = new Fridge();
        FoodItem eggs = new FoodItem("Eggs");

        fridge.addFoodItem(eggs, 4.25, Unit.PIECE);
        fridge.addFoodItem(eggs, new Amount(3.75, Unit.PIECE));

        assertEquals(8.0, fridge.getQuantity(eggs), DELTA);
    }

    /**
     * Verifies that querying an item not in the fridge returns zero.
     */
    @Test
    void testGetQuantityOfNonExistingFoodItem() {
        Fridge fridge = new Fridge();
        FoodItem cheese = new FoodItem("Cheese");

        assertEquals(0.0, fridge.getQuantity(cheese), DELTA);
        assertEquals(null, fridge.getUnit(cheese));
    }

    /**
     * Verifies that using part of an item's quantity succeeds and updates the remainder.
     */
    @Test
    void testUseFoodItem() {
        Fridge fridge = new Fridge();
        FoodItem bread = new FoodItem("Bread");

        fridge.addFoodItem(bread, 5.5, Unit.PIECE);

        boolean result = fridge.useFoodItem(bread, 2.25);

        assertTrue(result);
        assertEquals(3.25, fridge.getQuantity(bread), DELTA);
        assertEquals(Unit.PIECE, fridge.getUnit(bread));
    }

    /**
     * Verifies that using an item's exact quantity removes it from the fridge.
     */
    @Test
    void testUseExactQuantityRemovesFoodItem() {
        Fridge fridge = new Fridge();
        FoodItem butter = new FoodItem("Butter");

        fridge.addFoodItem(butter, 5.5, Unit.PIECE);

        boolean result = fridge.useFoodItem(butter, 5.5);

        assertTrue(result);
        assertEquals(0.0, fridge.getQuantity(butter), DELTA);
    }

    /**
     * Verifies that using more than the available quantity fails without changing it.
     */
    @Test
    void testCannotUseMoreThanAvailableQuantity() {
        Fridge fridge = new Fridge();
        FoodItem yogurt = new FoodItem("Yogurt");

        fridge.addFoodItem(yogurt, 3.5, Unit.LITRE);

        boolean result = fridge.useFoodItem(yogurt, 5.25);

        assertFalse(result);

        assertEquals(3.5, fridge.getQuantity(yogurt), DELTA);
    }

    /**
     * Verifies that using an item that is not in the fridge fails.
     */
    @Test
    void testCannotUseFoodItemThatDoesNotExist() {
        Fridge fridge = new Fridge();
        FoodItem chicken = new FoodItem("Chicken");

        boolean result = fridge.useFoodItem(chicken, 1.25);

        assertFalse(result);
        assertEquals(0.0, fridge.getQuantity(chicken), DELTA);
    }

    /**
     * Verifies that adding a negative quantity throws an exception.
     */
    @Test
    void testAddNegativeQuantityThrowsException() {
        Fridge fridge = new Fridge();
        FoodItem apples = new FoodItem("Apples");

        assertThrows(
            IllegalArgumentException.class,
            () -> fridge.addFoodItem(apples, -1.0, Unit.PIECE)
        );
    }

    /**
     * Verifies that using a negative quantity throws an exception and preserves stock.
     */
    @Test
    void testUseNegativeQuantityThrowsException() {
        Fridge fridge = new Fridge();
        FoodItem bananas = new FoodItem("Bananas");

        fridge.addFoodItem(bananas, 5.5, Unit.PIECE);

        assertThrows(
            IllegalArgumentException.class,
            () -> fridge.useFoodItem(bananas, -1.0)
        );

        assertEquals(5.5, fridge.getQuantity(bananas), DELTA);
    }

    /**
     * Verifies that using zero quantity does not change the stored amount.
     */
    @Test
    void testUsingZeroLeavesAmountUnchanged() {
        Fridge fridge = new Fridge();
        FoodItem rice = new FoodItem("Rice");
        fridge.addFoodItem(rice, 2.0, Unit.KILOGRAM);

        assertTrue(fridge.useFoodItem(rice, 0));
        assertEquals(2.0, fridge.getQuantity(rice), DELTA);
        assertEquals(Unit.KILOGRAM, fridge.getUnit(rice));
    }

    /**
     * Verifies that adding an existing item with another unit is rejected.
     */
    @Test
    void testAddingWithDifferentUnitThrowsException() {
        Fridge fridge = new Fridge();
        FoodItem flour = new FoodItem("Flour");
        fridge.addFoodItem(flour, 1.0, Unit.KILOGRAM);

        assertThrows(
            IllegalArgumentException.class,
            () -> fridge.addFoodItem(flour, 500.0, Unit.GRAM)
        );
        assertEquals(1.0, fridge.getQuantity(flour), DELTA);
    }

    /**
     * Verifies that null food items and amounts are rejected.
     */
    @Test
    void testNullFoodItemOrAmountThrowsException() {
        Fridge fridge = new Fridge();

        assertThrows(
            IllegalArgumentException.class,
            () -> fridge.addFoodItem(null, new Amount(1.0, Unit.PIECE))
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> fridge.addFoodItem(new FoodItem("Salt"), (Amount) null)
        );
    }

    /**
     * Verifies that NaN and infinite quantities are rejected.
     */
    @Test
    void testNonFiniteQuantityThrowsException() {
        Fridge fridge = new Fridge();
        FoodItem sugar = new FoodItem("Sugar");

        assertThrows(
            IllegalArgumentException.class,
            () -> fridge.addFoodItem(sugar, Double.NaN, Unit.GRAM)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> fridge.useFoodItem(sugar, Double.POSITIVE_INFINITY)
        );
    }

    /**
     * Verifies that hasItem reports whether enough quantity is available.
     */
    @Test
    void testHasItemChecksAvailableQuantity() {
        Fridge fridge = new Fridge();
        FoodItem oil = new FoodItem("Oil");
        fridge.addFoodItem(oil, 2.0, Unit.LITRE);

        assertTrue(fridge.hasItem(oil, 2.0));
        assertTrue(fridge.hasItem(oil, 1.5));
        assertFalse(fridge.hasItem(oil, 2.5));
        assertFalse(fridge.hasItem(new FoodItem("Vinegar"), 1.0));
    }

    /**
     * Verifies that modifying the returned map does not modify the fridge.
     */
    @Test
    void testGetFoodItemsReturnsCopy() {
        Fridge fridge = new Fridge();
        FoodItem beans = new FoodItem("Beans");
        fridge.addFoodItem(beans, 3.0, Unit.PIECE);

        Map<FoodItem, Amount> contents = fridge.getFoodItems();
        contents.clear();

        assertEquals(3.0, fridge.getQuantity(beans), DELTA);
    }
}