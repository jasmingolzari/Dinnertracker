package no.gr2631.dinnertracker.core;

/**
 * Represents measurement units used for quantites in ingredients and fridge items
 *
 * <p>Each unit holds a short symbol representation ("g" for GRAM) suitable for UI display and
 * persistence.
 */
public enum Unit {
  LITRE("L"),
  GRAM("g"),
  KILOGRAM("kg"),
  PIECE("stk"),
  TABLESPOON("ss"),
  TEASPOON("ts");

  private final String symbol;

  Unit(String symbol) {
    this.symbol = symbol;
  }

  public String getSymbol() {
    return symbol;
  }

  /**
   * Resolves a String to a corresponding {@link Unit} enum constant. The input string can be either
   * the enum name (e.g., "PIECE") or the user-facing symbol (e.g., "stk").
   *
   * @param value the serialized unit value, either as an enum name or as a symbol such as "stk"
   * @return the matching unit enum constant
   * @throws IllegalArgumentException if no matching unit exists
   */
  public static Unit parseUnit(String value) {
    for (Unit unit : Unit.values()) {
      if (unit.name().equalsIgnoreCase(value) || unit.getSymbol().equalsIgnoreCase(value)) {
        return unit;
      }
    }
    throw new IllegalArgumentException("Unsupported unit: " + value);
  }
}
