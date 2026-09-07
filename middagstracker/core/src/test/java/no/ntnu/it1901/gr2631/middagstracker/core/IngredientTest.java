package no.ntnu.it1901.gr2631.middagstracker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class IngredientTest {

    @Test
    void testGetName() {
        Ingredient ingredient = new Ingredient("Tomat");

        assertEquals("Tomat", ingredient.getName());
    }
}