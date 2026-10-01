package no.ntnu.it1901.gr2631.dinnertracker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Unit}.
 *
 * <p>The tests verify that all supported units have the expected symbols
 * and that the enum behaves correctly as a Java enum.</p>
 */
class UnitTest {

    /**
     * Verifies that {@link Unit#LITRE} has the expected symbol.
     */
    @Test
    void testLitreSymbol() {
        assertEquals("L", Unit.LITRE.getSymbol());
    }

    /**
     * Verifies that {@link Unit#GRAM} has the expected symbol.
     */
    @Test
    void testGramSymbol() {
        assertEquals("g", Unit.GRAM.getSymbol());
    }

    /**
     * Verifies that {@link Unit#KILOGRAM} has the expected symbol.
     */
    @Test
    void testKilogramSymbol() {
        assertEquals("kg", Unit.KILOGRAM.getSymbol());
    }

    /**
     * Verifies that {@link Unit#PIECE} has the expected symbol.
     */
    @Test
    void testPieceSymbol() {
        assertEquals("stk", Unit.PIECE.getSymbol());
    }

    /**
     * Verifies that {@link Unit#Tablespoon} has the expected symbol.
     */
    @Test
    void testTablespoonSymbol() {
        assertEquals("ss", Unit.TABLESPOON.getSymbol());
    }

    /**
     * Verifies that {@link Unit#TEASPOON} has the expected symbol.
     */
    @Test
    void testTeaspoonSymbol() {
        assertEquals("ts", Unit.TEASPOON.getSymbol());
    }

    /**
     * Verifies that every defined unit has a non-null and non-empty symbol.
     */
    @Test
    void testAllUnitsHaveValidSymbols() {
        for (Unit unit : Unit.values()) {
            assertNotNull(unit.getSymbol());
            assertEquals(false, unit.getSymbol().isEmpty());
        }
    }

    /**
     * Verifies that all currently supported units are present.
     */
    @Test
    void testExpectedNumberOfUnits() {
        assertEquals(6, Unit.values().length);
    }

    /**
     * Verifies that {@link Unit#valueOf(String)} returns the correct enum
     * constant for valid names.
     */
    @Test
    void testValueOfValidUnits() {
        assertSame(Unit.LITRE, Unit.valueOf("LITRE"));
        assertSame(Unit.GRAM, Unit.valueOf("GRAM"));
        assertSame(Unit.KILOGRAM, Unit.valueOf("KILOGRAM"));
        assertSame(Unit.PIECE, Unit.valueOf("PIECE"));
        assertSame(Unit.TABLESPOON, Unit.valueOf("TABLESPOON"));
        assertSame(Unit.TEASPOON, Unit.valueOf("TEASPOON"));
    }

    /**
     * Verifies that {@link Unit#valueOf(String)} throws an exception for
     * an invalid unit name.
     */
    @Test
    void testValueOfInvalidUnit() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Unit.valueOf("INVALID")
        );
    }

    /**
     * Verifies that {@link Unit#valueOf(String)} is case-sensitive.
     */
    @Test
    void testValueOfIsCaseSensitive() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Unit.valueOf("gram")
        );
    }

    /**
     * Verifies that {@link Unit#valueOf(String)} rejects a null value.
     */
    @Test
    void testValueOfNull() {
        assertThrows(
            NullPointerException.class,
            () -> Unit.valueOf(null)
        );
    }

    /**
     * Verifies that the symbol returned by {@link Unit#getSymbol()} remains
     * stable when accessed multiple times.
     */
    @Test
    void testSymbolIsStable() {
        for (Unit unit : Unit.values()) {
            assertSame(unit.getSymbol(), unit.getSymbol());
        }
    }
}