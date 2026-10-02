package no.gr2631.dinnertracker.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.Unit;

/**
 * Handles reading and writing of recipes to a JSON file. All recipes are stored together in a
 * single file as a JSON file formatted as an array. The conversion between {@link Recipe} objects
 * and JSON is done manually, since a recipe stores its ingredients in a {@code Map<FoodItem,
 * Amount>}, which Jackson cannot handle automatically in a useful way.
 */
public class RecipeFileHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Loads all recipes from the given file. If the file does not exist or is empty, an empty list is
   * returned.
   *
   * @param path the path to the JSON file to read from
   * @return a modifiable list containing all recipes in the file, or an empty list if the file does
   *     not exist or is empty
   * @throws IOException if the file cannot be read or contains invalid JSON
   */
  public List<Recipe> loadAll(Path path) throws IOException {
    List<Recipe> recipes = new ArrayList<>();

    if (!path.toFile().exists() || path.toFile().length() == 0) {
      return recipes;
    }

    JsonNode root = objectMapper.readTree(path.toFile());
    for (JsonNode recipeNode : root) {
      recipes.add(fromJson(recipeNode));
    }
    return recipes;
  }

  /**
   * Loads a single recipe with the given name from the file. This reads the entire file every time
   * it is called.
   *
   * @param path the path to the JSON file to read from
   * @param name the name of the recipe to find
   * @return the recipe with the given name
   * @throws IOException if the file cannot be read or contains invalid JSON
   * @throws IllegalArgumentException if no recipe with the given name exists in the file
   */
  public Recipe loadRecipe(Path path, String name) throws IOException {
    for (Recipe recipe : loadAll(path)) {
      if (recipe.getName().equals(name)) {
        return recipe;
      }
    }
    throw new IllegalArgumentException("Fant ingen oppskrift med navn: " + name);
  }

  /**
   * Adds a recipe to the file without removing the existing recipes. The existing recipes are
   * loaded, the new recipe is appended to the list, and the whole list is written back to the file.
   *
   * @param recipe the recipe to add
   * @param path the path to the JSON file to add the recipe to
   * @throws IOException if the file cannot be read or written
   */
  public void addRecipe(Recipe recipe, Path path) throws IOException {
    List<Recipe> recipes = loadAll(path);
    recipes.add(recipe);
    saveAll(recipes, path);
  }

  /**
   * Writes the given recipes to the file as a JSON array. Any existing content in the file is
   * overwritten.
   *
   * @param recipes the recipes to save
   * @param path the path to the JSON file to write to
   * @throws IOException if the file cannot be written
   */
  public void saveAll(List<Recipe> recipes, Path path) throws IOException {
    ArrayNode array = objectMapper.createArrayNode();
    for (Recipe recipe : recipes) {
      array.add(toJson(recipe));
    }
    objectMapper.writeValue(path.toFile(), array);
  }

  /**
   * Converts a recipe to a JSON object with the fields {@code name}, {@code ingredients} (each with
   * {@code name}, {@code quantity} and {@code unit}) and {@code steps}.
   *
   * @param recipe the recipe to convert
   * @return a JSON object representing the recipe
   */
  private ObjectNode toJson(Recipe recipe) {
    ObjectNode node = objectMapper.createObjectNode();
    node.put("name", recipe.getName());

    ArrayNode ingredientsNode = node.putArray("ingredients");
    for (Map.Entry<FoodItem, Amount> entry : recipe.getIngredients().entrySet()) {
      ObjectNode ingredientNode = ingredientsNode.addObject();
      ingredientNode.put("name", entry.getKey().getName());
      ingredientNode.put("quantity", entry.getValue().quantity());
      ingredientNode.put("unit", entry.getValue().unit().name());
    }

    ArrayNode stepsNode = node.putArray("steps");
    for (String step : recipe.getSteps()) {
      stepsNode.add(step);
    }
    return node;
  }

  /**
   * Creates a recipe from a JSON object in the format produced by {@link #toJson(Recipe)}.
   *
   * @param recipeNode the JSON object to convert
   * @return the recipe described by the JSON object
   * @throws NullPointerException if a required field is missing from the JSON
   * @throws IllegalArgumentException if the unit name does not match a {@link Unit}, or if the
   *     recipe name is blank
   */
  private Recipe fromJson(JsonNode recipeNode) {
    Recipe recipe = new Recipe(recipeNode.get("name").asText());

    for (JsonNode ingredientNode : recipeNode.get("ingredients")) {
      FoodItem item = new FoodItem(ingredientNode.get("name").asText());
      Amount amount =
          new Amount(
              ingredientNode.get("quantity").asDouble(),
              Unit.valueOf(ingredientNode.get("unit").asText()));
      recipe.addIngredient(item, amount);
    }

    for (JsonNode stepNode : recipeNode.get("steps")) {
      recipe.addStep(stepNode.asText());
    }
    return recipe;
  }
}
