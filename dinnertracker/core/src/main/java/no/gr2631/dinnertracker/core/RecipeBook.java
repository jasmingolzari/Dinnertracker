package no.gr2631.dinnertracker.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeBook {
    private List<Recipe> recipes;

    public RecipeBook() {
        this.recipes = new ArrayList<>();
    }

    public void addRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Oppskriften kan ikke være null.");
        }
        recipes.add(recipe);
    }

    public List<Recipe> getRecipes() {
        return Collections.unmodifiableList(recipes);
    }

    
}
