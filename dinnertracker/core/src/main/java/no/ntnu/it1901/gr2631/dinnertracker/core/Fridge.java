package no.ntnu.it1901.gr2631.dinnertracker.core;

import java.util.HashMap;

public class Fridge {
    private static final HashMap<FoodItem, Integer> foodItems = new HashMap<>();

    /**
     * Validate that the quantity is not negative.
     * @param quantity The quantity to validate.
     * @throws IllegalArgumentException if the quantity is negative.
     */
    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
    }

    /**
     * Add a food item to the fridge.
     * @param foodItem The food item to add.
     * @param quantity The quantity to add.
     * Uses a HashMap to store the food items and their quantities. 
     * If the food item already exists in the fridge, the quantity is updated.
     */
    public static void addFoodItem(FoodItem foodItem, int quantity) {
        validateQuantity(quantity);
        if (foodItems.containsKey(foodItem)) {
            int currentQuantity = foodItems.get(foodItem);
            foodItems.put(foodItem, currentQuantity + quantity);
        } else {
            foodItems.put(foodItem, quantity);
        }
    }

    /**
     * Use a food item from the fridge.
     * @param foodItem The food item to use.
     * @param quantity The quantity to use.
     * @return true if the food item was successfully used,
     *  false if the quantity is more than the available quantity
     *  or if the food item does not exist in the fridge.
     */
    public static boolean useFoodItem(FoodItem foodItem, int quantity) {
        validateQuantity(quantity);
        Integer currentQuantity = foodItems.get(foodItem);
        if (currentQuantity == null || currentQuantity < quantity) {
            return false;
        }
        if (currentQuantity == quantity) {
            foodItems.remove(foodItem);
        } 
        else {
            foodItems.put(foodItem, currentQuantity - quantity);
        }
        return true;
    }

    /**
     * Get the quantity of a food item in the fridge.
     * @param foodItem
     * @return int quantity of the food item in the fridge, or 0 if the food item does not exist.
     */
    public static int getQuantity(FoodItem foodItem) {
        return foodItems.getOrDefault(foodItem, 0);
    }

}
