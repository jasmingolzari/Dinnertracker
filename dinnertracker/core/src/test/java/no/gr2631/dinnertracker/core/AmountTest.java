package no.gr2631.dinnertracker.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests the functionality and validation of {@link Amount}. Tests valid quantities and units, as
 * well as invalid quantities, infinite values and null units.
 */
class AmountTest {

  /** Tests that a valid positive quantity and unit can be used to create an Amount. */
  @Test
  void shouldCreateAmountWithValidValues() {
    Amount amount = new Amount(5.0, Unit.GRAM);

    assertEquals(5.0, amount.quantity());
    assertEquals(Unit.GRAM, amount.unit());
  }

  /** Tests that zero is not accepted as a quantity. */
  @Test
  void shouldRejectZeroQuantity() {
    assertThrows(IllegalArgumentException.class, () -> new Amount(0, Unit.GRAM));
  }

  /** Tests that negative quantities are not accepted. */
  @Test
  void shouldRejectNegativeQuantity() {
    assertThrows(IllegalArgumentException.class, () -> new Amount(-5.0, Unit.GRAM));
  }

  /** Tests that positive infinity is not accepted as a quantity. */
  @Test
  void shouldRejectPositiveInfinity() {
    assertThrows(
        IllegalArgumentException.class, () -> new Amount(Double.POSITIVE_INFINITY, Unit.GRAM));
  }

  /** Tests that negative infinity is not accepted as a quantity. */
  @Test
  void shouldRejectNegativeInfinity() {
    assertThrows(
        IllegalArgumentException.class, () -> new Amount(Double.NEGATIVE_INFINITY, Unit.GRAM));
  }

  /** Tests that NaN is not accepted as a quantity. */
  @Test
  void shouldRejectNanQuantity() {
    assertThrows(IllegalArgumentException.class, () -> new Amount(Double.NaN, Unit.GRAM));
  }

  /** Tests that a null unit is not accepted. */
  @Test
  void shouldRejectNullUnit() {
    assertThrows(IllegalArgumentException.class, () -> new Amount(5.0, null));
  }

  /** Tests that a very small positive finite quantity is accepted. */
  @Test
  void shouldAcceptVerySmallPositiveQuantity() {
    assertDoesNotThrow(() -> new Amount(Double.MIN_VALUE, Unit.GRAM));
  }
}
