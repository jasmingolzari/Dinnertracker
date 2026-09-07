package no.ntnu.it1901.gr2631.middagstracker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class FoodItemTest {

    @Test
    void testGetName() {
        FoodItem ingredient = new FoodItem("Tomat");

        assertEquals("Tomat", ingredient.getName());
    }
}