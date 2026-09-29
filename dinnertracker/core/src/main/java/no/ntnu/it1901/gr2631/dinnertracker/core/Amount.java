package no.ntnu.it1901.gr2631.dinnertracker.core;

public record Amount(double quantity, Unit unit) {

    public Amount {
        if (Double.isFinite(quantity) || quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be positive finite number "
            );
        }

        if (unit == null) {
            throw new IllegalArgumentException(
                "Unit cannot be null"
            );
        }
    }
}