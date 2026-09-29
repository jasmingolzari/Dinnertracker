package no.ntnu.it1901.gr2631.dinnertracker.core;

import java.util.HashMap;
import java.util.Map;

public class Fridge {
    private final HashMap<FoodItem, Amount> foodItems;

    public Fridge() {
        this.foodItems = new HashMap<>();
    }

    /**
     * Validate that the quantity is not negative.
     * @param quantity The quantity to validate.
     * @throws IllegalArgumentException if the quantity is negative.
     */
    private static void validateQuantity(double quantity) {
        if (!Double.isFinite(quantity) || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be finite and non-negative.");
        }
    }

    /**
     * Add a food item to the fridge.
     * @param foodItem The food item to add.
     * @param quantity The quantity to add.
     * Uses a HashMap to store the food items and their quantities. 
     * If the food item already exists in the fridge, the quantity is updated.
     */
    public void addFoodItem(FoodItem foodItem, double quantity, Unit unit) {
        addFoodItem(foodItem, new Amount(quantity, unit));
    }

    /**
     * Add an amount of a food item to the fridge.
     *
     * @param foodItem the food item to add
     * @param amount the amount to add
     * @throws IllegalArgumentException if the item or amount is null, or if
     *                                  the units do not match an existing item
     */
    public void addFoodItem(FoodItem foodItem, Amount amount) {
        if (foodItem == null || amount == null) {
            throw new IllegalArgumentException("Food item and amount cannot be null.");
        }

        Amount currentAmount = foodItems.get(foodItem);
        if (currentAmount == null) {
            foodItems.put(foodItem, amount);
            return;
        }

        if (currentAmount.unit() != amount.unit()) {
            throw new IllegalArgumentException("Units must match.");
        }

        foodItems.put(
            foodItem,
            new Amount(currentAmount.quantity() + amount.quantity(), amount.unit())
        );
    }

    /**
     * Use a food item from the fridge.
     * @param foodItem The food item to use.
     * @param quantity The quantity to use.
     * @return true if the food item was successfully used,
     *  false if the quantity is more than the available quantity
     *  or if the food item does not exist in the fridge.
     */
    public boolean useFoodItem(FoodItem foodItem, double quantity) {
        validateQuantity(quantity);
        Amount currentAmount = foodItems.get(foodItem);
        if (currentAmount == null || currentAmount.quantity() < quantity) {
            return false;
        }

        if (currentAmount.quantity() == quantity) {
            foodItems.remove(foodItem);
        } else if (quantity > 0) {
            foodItems.put(
                foodItem,
                new Amount(currentAmount.quantity() - quantity, currentAmount.unit())
            );
        }
        return true;
    }

    /**
     * Get the quantity of a food item in the fridge.
     * @param foodItem
     * @return double quantity of the food item in the fridge, or 0 if the food item does not exist.
     */
    public double getQuantity(FoodItem foodItem) {
        Amount amount = foodItems.get(foodItem);
        return amount == null ? 0.0 : amount.quantity();
    }

    /**
     * Get the unit of a food item in the fridge.
     * @param foodItem
     * @return Unit of the food item in the fridge, or null if the food item does not exist.
     */
    public Unit getUnit(FoodItem foodItem) {
        Amount amount = foodItems.get(foodItem);
        return amount == null ? null : amount.unit();
    }

    /**
     * Check whether the fridge contains enough of a food item.
     *
     * @param foodItem the food item to check
     * @param quantity the required quantity
     * @param unit the unit of the required quantity
     * @return true if enough of the item is available
     */
    public boolean hasItem(FoodItem foodItem, double quantity) {
        validateQuantity(quantity);
        return getQuantity(foodItem) >= quantity;
    }

    /**
     * Get the food items and their amounts.
     *
     * @return a copy of the fridge contents
     */
    public Map<FoodItem, Amount> getFoodItems() {
        return new HashMap<>(foodItems);
    }



}
