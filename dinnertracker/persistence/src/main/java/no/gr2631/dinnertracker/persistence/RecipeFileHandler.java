package no.gr2631.dinnertracker.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.User;

/**
 * Handles reading and writing of recipes to a JSON file. All recipes are stored together in a
 * single file as a JSON file formatted as an array. The conversion between {@link Recipe} objects
 * and JSON is done manually, since a recipe stores its ingredients in a {@code Map<FoodItem,
 * Amount>}, which Jackson cannot handle automatically in a useful way.
 */
public class RecipeFileHandler implements GenericRepository<RecipeBook> {

  private final ObjectMapper objectMapper;

  public RecipeFileHandler() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  /**
   * Loads all recipes from the given file. If the file does not exist or is empty, an empty list is
   * returned.
   *
   * @param path the path to the JSON file to read from
   * @return a modifiable list containing all recipes in the file, or an empty list if the file does
   *     not exist or is empty
   * @throws IOException if the file cannot be read or contains invalid JSON
   */
  @Override
  public RecipeBook load(Path path, User userName) throws IOException {
    RecipeBook recipeBook = new RecipeBook(userName);

    if (!path.toFile().exists() || path.toFile().length() == 0) {
      return recipeBook;
    }

    JsonNode root = objectMapper.readTree(path.toFile());
    if (root == null || root.isNull()) {
      return recipeBook;
    }

    for (JsonNode recipeNode : root) {
      if (recipeNode == null || recipeNode.isNull()) {
        continue;
      }
      recipeBook.addRecipe(fromJson(recipeNode));
    }
    return recipeBook;
  }

  /**
   * Writes the recipes in the given recipebook to the file as a JSON array. Any existing content in
   * the file is overwritten.
   *
   * @param recipebook the recipebook to save
   * @param path the path to the JSON file to write to
   * @throws IOException if the file cannot be written
   */
  @Override
  public void save(Path path, RecipeBook recipebook) throws IOException {
    ArrayNode array = objectMapper.createArrayNode();
    for (Recipe recipe : recipebook.getRecipes()) {
      array.add(toJson(recipe, recipe.getUserName()));
    }
    objectMapper.writeValue(path.toFile(), array);
  }

  /**
   * Converts a recipe to a JSON object with the fields {@code name}, {@code ingredients} (each with
   * {@code name}, {@code quantity} and {@code unit}) and {@code steps}.
   *
   * @param recipe the recipe to convert
   * @param userName the user associated with the recipe
   * @return a JSON object representing the recipe
   */
  private ObjectNode toJson(Recipe recipe, User userName) {
    ObjectNode node = objectMapper.createObjectNode();
    node.put("userName", userName.getUserName());
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
    String recipeName = recipeNode.get("name").asText();
    User userName = new User(recipeNode.get("userName").asText());
    Recipe recipe = new Recipe(recipeName, userName);

    JsonNode ingredientsNode = recipeNode.get("ingredients");
    if (ingredientsNode != null && !ingredientsNode.isNull()) {
      for (JsonNode ingredientNode : ingredientsNode) {
        if (ingredientNode == null || ingredientNode.isNull()) {
          continue;
        }
        FoodItem item = new FoodItem(ingredientNode.get("name").asText());
        Amount amount =
            new Amount(
                ingredientNode.get("quantity").asDouble(),
                Unit.parseUnit(ingredientNode.get("unit").asText()));
        recipe.addIngredient(item, amount);
      }
    }

    JsonNode stepsNode = recipeNode.get("steps");
    if (stepsNode != null && !stepsNode.isNull()) {
      for (JsonNode stepNode : stepsNode) {
        if (stepNode != null && !stepNode.isNull()) {
          recipe.addStep(stepNode.asText());
        }
      }
    }
    return recipe;
  }
}
