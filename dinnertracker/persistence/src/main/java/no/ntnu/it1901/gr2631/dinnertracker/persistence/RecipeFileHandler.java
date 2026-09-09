package no.ntnu.it1901.gr2631.dinnertracker.persistence;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.ntnu.it1901.gr2631.dinnertracker.core.Recipe;

public class RecipeFileHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveRecipe(Recipe recipe, Path path) throws IOException {
        objectMapper.writeValue(path.toFile(), recipe);
    }
}