package no.ntnu.it1901.gr2631.dinnertracker.core;

/**
 * Represents measurement units used for quantites in ingredients and fridge items 
 * <p>
 * Each unit holds a short symbol representation ("g" for GRAM) suitable for UI display 
 * and persistence. 
 * </p>
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

}

 