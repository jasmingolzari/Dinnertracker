package no.gr2631.dinnertracker.core;

import java.util.Locale;

/** Represents a food item in our dinner tracker. */
public final class FoodItem {
  private final String name;

  /**
   * Creates a new food item.
   *
   * @param name the name of the food item
   * @throws IllegalArgumentException if the name is null or blank
   */
  public FoodItem(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Du må oppgi et navn for matvaren.");
    }

    String normalizedName = name.trim();
    if (normalizedName.isBlank()) {
      throw new IllegalArgumentException("Du må oppgi et navn for matvaren.");
    }

    this.name = normalizedName;
  }

  /**
   * Gets the name of the food item.
   *
   * @return the name of the food item
   */
  public String getName() {
    return name;
  }

  /**
   * Checks whether two food items are equal. Food item names are compared without considering
   * capitalization.
   *
   * @param obj the object to compare with
   * @return true if the food items have the same name, ignoring case
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof FoodItem)) {
      return false;
    }

    FoodItem other = (FoodItem) obj;
    return name.equalsIgnoreCase(other.name.trim());
  }

  /**
   * Generates a hash code based on the food item's name. The hash code is case-insensitive to match
   * equals().
   *
   * @return the hash code of the food item
   */
  @Override
  public int hashCode() {
    return name.toLowerCase(Locale.ROOT).hashCode();
  }
}
