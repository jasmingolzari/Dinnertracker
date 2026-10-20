package no.gr2631.dinnertracker.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores food items and their amounts in a fridge associated with a user. Each food item has one
 * stored quantity and unit.
 */
public class Fridge {
  private final Map<FoodItem, Amount> foodItems;
  private final User userName;

  /**
   * Creates an empty fridge associated with the given user.
   *
   * @param userName the associated user, which may be null
   */
  public Fridge(User userName) {
    this.userName = userName;
    this.foodItems = new HashMap<>();
  }

  /**
   * Validates that a quantity is finite and non-negative.
   *
   * @param quantity the quantity to validate
   * @throws IllegalArgumentException if the quantity is negative, infinite, or NaN
   */
  private static void validateQuantity(double quantity) {
    if (!Double.isFinite(quantity) || quantity < 0) {
      throw new IllegalArgumentException("Mengden må være endelig og ikke-negativ.");
    }
  }

  /**
   * Adds an amount of a food item to the fridge. If the item is already stored, the quantities are
   * added together, provided their units match.
   *
   * @param foodItem the food item to add
   * @param amount the amount to add
   * @throws IllegalArgumentException if the item or amount is null, the quantity is negative or
   *     non-finite, or the unit differs from the stored unit
   */
  public void addFoodItem(FoodItem foodItem, Amount amount) {
    if (foodItem == null || amount == null) {
      throw new IllegalArgumentException("Matvare eller mengde kan ikke være null.");
    }

    validateQuantity(amount.quantity());

    Amount currentAmount = foodItems.get(foodItem);
    if (currentAmount == null) {
      foodItems.put(foodItem, amount);
      return;
    }

    if (currentAmount.unit() != amount.unit()) {
      throw new IllegalArgumentException(
          "Enheter for "
              + foodItem.getName()
              + " stemmer ikke overens. Forventet: "
              + currentAmount.unit().getSymbol()
              + ", men fikk: "
              + amount.unit().getSymbol());
    }

    foodItems.put(
        foodItem, new Amount(currentAmount.quantity() + amount.quantity(), amount.unit()));
  }

  /**
   * Removes the requested amount of a food item from the fridge. The item must be stored in a
   * sufficient quantity and with the requested unit. If the entire stored quantity is used, the
   * item is removed from the fridge.
   *
   * @param foodItem the food item to use
   * @param amount the amount to use
   * @throws IllegalArgumentException if the item is missing, the amount is null, the requested
   *     quantity is negative or non-finite, the units differ, or the stored quantity is
   *     insufficient
   * @throws NullPointerException if the food item is null
   */
  public void useFoodItem(FoodItem foodItem, Amount amount) {
    Amount currentAmount = foodItems.get(foodItem);
    if (currentAmount == null || !hasItem(foodItem, amount)) {
      throw new IllegalArgumentException(
          "Du kan ikke bruke"
              + foodItem.getName()
              + ", det er ikke nok tilgjengelig i kjøleskapet.");
    }

    if (currentAmount.quantity() == amount.quantity()) {
      foodItems.remove(foodItem);
    } else if (amount.quantity() > 0) {
      foodItems.put(
          foodItem, new Amount(currentAmount.quantity() - amount.quantity(), currentAmount.unit()));
    }
  }

  /**
   * Checks whether the fridge contains the requested quantity of a food item with the same unit.
   *
   * @param foodItem the food item to check
   * @param amount the amount to check
   * @return true if the item exists with a matching unit and sufficient quantity; false otherwise
   * @throws IllegalArgumentException if the food item or amount is null, or the requested quantity
   *     is negative or non-finite
   */
  private boolean hasItem(FoodItem foodItem, Amount amount) {
    if (foodItem == null || amount == null) {
      throw new IllegalArgumentException("Mat eller mengde kan ikke være null.");
    }
    validateQuantity(amount.quantity());
    if (!foodItems.containsKey(foodItem)) {
      return false;
    }
    return getQuantity(foodItem) >= amount.quantity() && getUnit(foodItem) == amount.unit();
  }

  /**
   * Gets the stored quantity of a food item.
   *
   * @param foodItem the food item to look up
   * @return the stored quantity, or zero if the item is absent or null
   */
  public double getQuantity(FoodItem foodItem) {
    Amount amount = foodItems.get(foodItem);
    return amount == null ? 0.0 : amount.quantity();
  }

  /**
   * Gets the unit used for a stored food item.
   *
   * @param foodItem the food item to look up
   * @return the stored unit, or null if the item is absent or null
   */
  public Unit getUnit(FoodItem foodItem) {
    Amount amount = foodItems.get(foodItem);
    return amount == null ? null : amount.unit();
  }

  /**
   * Gets a shallow copy of the food items and their amounts. Adding or removing entries in the
   * returned map does not change the fridge contents.
   *
   * @return a copy of the fridge contents
   */
  public Map<FoodItem, Amount> getFoodItems() {
    return new HashMap<>(foodItems);
  }

  /**
   * Gets the user associated with this fridge.
   *
   * @return the associated user, or null if no user was supplied
   */
  public User getUserName() {
    return userName;
  }
}
