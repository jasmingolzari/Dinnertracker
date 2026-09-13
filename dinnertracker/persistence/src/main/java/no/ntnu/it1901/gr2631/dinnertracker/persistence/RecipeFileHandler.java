package no.ntnu.it1901.gr2631.dinnertracker.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.ntnu.it1901.gr2631.dinnertracker.core.FoodItem;
import no.ntnu.it1901.gr2631.dinnertracker.core.Recipe;

public class RecipeFileHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveRecipe(Recipe recipe, Path path) throws IOException {
        objectMapper.writeValue(path.toFile(), recipe);
    }

    public Recipe loadRecipe(Path path) throws IOException {
        JsonNode root = objectMapper.readTree(path.toFile());

        String name = root.get("name").asText();

        List<FoodItem> ingridients = new ArrayList<>();
        JsonNode ingridientsNode = root.get("ingridients");

        for (JsonNode ingridientNode : ingridientsNode) {
            String foodItemName = ingridientNode.get("name").asText();
            ingridients.add(new FoodItem(foodItemName));
        }

        List<String> steps = new ArrayList<>();
        JsonNode stepsNode = root.get("steps");

        for (JsonNode stepNode : stepsNode) {
            steps.add(stepNode.asText());
        }

        return new Recipe(name, ingridients, steps);
    }
}