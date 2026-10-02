package no.gr2631.dinnertracker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/** Tests the functionality of the FoodItem class. */
public class FoodItemTest {

  /** Tests that the correct name is returned. */
  @Test
  void testGetName() {
    FoodItem ingredient = new FoodItem("Tomat");

    assertEquals("Tomat", ingredient.getName());
  }

  /** Tests that two food items with the same name are considered equal. */
  @Test
  void testFoodItemsWithSameNameAreEqual() {
    FoodItem tomato1 = new FoodItem("Tomat");
    FoodItem tomato2 = new FoodItem("Tomat");

    assertEquals(tomato1, tomato2);
  }

  /** Tests that food items with different names are not considered equal. */
  @Test
  void testFoodItemsWithDifferentNamesAreNotEqual() {
    FoodItem tomato = new FoodItem("Tomat");
    FoodItem milk = new FoodItem("Melk");

    assertNotEquals(tomato, milk);
  }

  /** Tests that food item names are compared without considering capitalization. */
  @Test
  void testFoodItemsIgnoreCase() {
    FoodItem tomato1 = new FoodItem("Tomat");
    FoodItem tomato2 = new FoodItem("TOMAT");

    assertEquals(tomato1, tomato2);
  }

  /** Tests that a null name is not allowed. */
  @Test
  void testNullNameThrowsException() {
    try {
      new FoodItem(null);
    } catch (IllegalArgumentException e) {
      return;
    }

    throw new AssertionError("Expected IllegalArgumentException");
  }

  /** Tests that a blank name is not allowed. */
  @Test
  void testBlankNameThrowsException() {
    try {
      new FoodItem("   ");
    } catch (IllegalArgumentException e) {
      return;
    }

    throw new AssertionError("Expected IllegalArgumentException");
  }

  /** Tests that equal food items have the same hash code. */
  @Test
  void testEqualFoodItemsHaveSameHashCode() {
    FoodItem tomato1 = new FoodItem("Tomat");
    FoodItem tomato2 = new FoodItem("TOMAT");

    assertEquals(tomato1.hashCode(), tomato2.hashCode());
  }
}
