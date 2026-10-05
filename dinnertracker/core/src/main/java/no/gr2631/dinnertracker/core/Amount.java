package no.gr2631.dinnertracker.core;

/**
 * Represents an immutable quantity combined with a measurement unit
 *
 * <p>Ensures that the quantity is a positive finite number, and that the unit is not {@code null}.
 * Amount
 *
 * @param quantity the numerical amount, must be greater than zero and finite
 * @param unit the measurement unit (g,kg), cannot be null
 */
public record Amount(double quantity, Unit unit) {

  /**
   * Constructor that validates negative quantites, and null-units
   *
   * @param quantity
   * @param unit
   */
  public Amount {
    if (!Double.isFinite(quantity) || quantity <= 0) {
      throw new IllegalArgumentException("Mengden må være endelig og større enn null.");
    }

    if (unit == null) {
      throw new IllegalArgumentException("Enhet kan ikke være null.");
    }
  }
}
