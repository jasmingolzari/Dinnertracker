package no.gr2631.dinnertracker.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.User;
import no.gr2631.dinnertracker.persistence.RecipeFileHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** REST endpoints for reading and creating recipes. */
@RestController
public class RecipeRestController {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${dinnertracker.recipes.path}")
  private String recipesPath;

  private final RecipeFileHandler recipeFileHandler = new RecipeFileHandler();

  /** Returns a basic service health message. */
  @GetMapping("/health")
  public String health() {
    return "Dinnertracker REST API is running";
  }

  /**
   * Returns recipes belonging to the requested user.
   *
   * @param userName the user whose recipes should be returned
   * @return the user's recipes as JSON
   */
  @GetMapping(value = "/recipes", produces = MediaType.APPLICATION_JSON_VALUE)
  public String getRecipes(@RequestParam(name = "userName", required = false) String userName) {
    if (userName == null || userName.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userName is required");
    }
    ArrayNode recipesJson = objectMapper.createArrayNode();
    for (Recipe recipe : loadAllRecipes()) {
      if (recipe.getUserName().getUserName().equals(userName)) {
        recipesJson.add(recipeToJson(recipe));
      }
    }
    return recipesJson.toString();
  }

  /**
   * Returns every recipe, regardless of who created it. Recipes are shared between all users, so
   * this is what the JavaFX client shows in the recipe list. Each recipe keeps its {@code userName}
   * so the client can show who added it.
   *
   * @return all recipes as JSON
   */
  @GetMapping(value = "/recipes/all", produces = MediaType.APPLICATION_JSON_VALUE)
  public String getAllRecipes() {
    ArrayNode recipesJson = objectMapper.createArrayNode();
    for (Recipe recipe : loadAllRecipes()) {
      recipesJson.add(recipeToJson(recipe));
    }
    return recipesJson.toString();
  }

  /**
   * Loads every recipe stored in the recipe file.
   *
   * @return all stored recipes
   * @throws ResponseStatusException with status 500 if the file cannot be read
   */
  private List<Recipe> loadAllRecipes() {
    try {
      // The file handler loads every recipe in the file; the user only names the returned book.
      RecipeBook recipeBook = recipeFileHandler.load(Path.of(recipesPath), new User(""));
      return recipeBook.getRecipes();
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Could not load recipes", e);
    }
  }

  /**
   * Converts a recipe to the JSON format used by the recipe endpoints.
   *
   * @param recipe the recipe to convert
   * @return the recipe as a JSON object
   */
  private ObjectNode recipeToJson(Recipe recipe) {
    ObjectNode recipeJson = objectMapper.createObjectNode();
    recipeJson.put("userName", recipe.getUserName().getUserName());
    recipeJson.put("name", recipe.getName());
    ArrayNode ingredientsJson = recipeJson.putArray("ingredients");
    for (Map.Entry<FoodItem, Amount> entry : recipe.getIngredients().entrySet()) {
      ObjectNode ingredientJson = ingredientsJson.addObject();
      ingredientJson.put("name", entry.getKey().getName());
      ingredientJson.put("quantity", entry.getValue().quantity());
      ingredientJson.put("unit", entry.getValue().unit().name());
    }
    ArrayNode stepsJson = recipeJson.putArray("steps");
    for (String step : recipe.getSteps()) {
      stepsJson.add(step);
    }
    return recipeJson;
  }

  /**
   * Validates and stores a recipe submitted as JSON.
   *
   * @param requestBody the recipe payload
   * @return the result of creating the recipe
   */
  @PostMapping("/recipes")
  public ResponseEntity<String> createRecipe(@RequestBody String requestBody) {
    JsonNode requestJson;

    try {
      requestJson = objectMapper.readTree(requestBody);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      return ResponseEntity.badRequest().body("Invalid JSON");
    }
    if (requestJson == null || !requestJson.isObject()) {
      return ResponseEntity.badRequest().body("JSON object is required");
    }
    if (!requestJson.has("userName") || !requestJson.has("name")) {
      return ResponseEntity.badRequest().body("userName and name are required");
    }
    if (!requestJson.path("userName").isTextual() || !requestJson.path("name").isTextual()) {
      return ResponseEntity.badRequest().body("userName and name must be strings");
    }
    String userName = requestJson.path("userName").asText();
    String recipeName = requestJson.path("name").asText();

    if (userName.isBlank() || recipeName.isBlank()) {
      return ResponseEntity.badRequest().body("userName and name are required");
    }
    User user = new User(userName);
    Recipe recipe = new Recipe(recipeName, user);
    JsonNode ingredientsJson = requestJson.path("ingredients");

    if (!ingredientsJson.isMissingNode() && !ingredientsJson.isArray()) {
      return ResponseEntity.badRequest().body("ingredients must be an array");
    }
    if (ingredientsJson.isArray()) {
      for (JsonNode ingredientJson : ingredientsJson) {
        if (ingredientJson == null || !ingredientJson.isObject()) {
          return ResponseEntity.badRequest().body("Each ingredient must be an object");
        }
        if (!ingredientJson.has("name")
            || !ingredientJson.has("quantity")
            || !ingredientJson.has("unit")) {
          return ResponseEntity.badRequest()
              .body("Ingredient name, quantity and unit are required");
        }
        if (!ingredientJson.path("name").isTextual()
            || !ingredientJson.path("quantity").isNumber()
            || !ingredientJson.path("unit").isTextual()) {
          return ResponseEntity.badRequest().body("Invalid ingredient field types");
        }
        String ingredientName = ingredientJson.path("name").asText();

        if (ingredientName.isBlank()) {
          return ResponseEntity.badRequest().body("Ingredient name must not be blank");
        }
        double quantity = ingredientJson.path("quantity").asDouble();

        if (!Double.isFinite(quantity) || quantity <= 0) {
          return ResponseEntity.badRequest().body("Ingredient quantity must be greater than zero");
        }
        String unitName = ingredientJson.path("unit").asText();

        if (unitName.isBlank()) {
          return ResponseEntity.badRequest().body("Ingredient unit must not be blank");
        }
        try {
          Unit.parseUnit(unitName);
        } catch (IllegalArgumentException e) {
          return ResponseEntity.badRequest().body("Unsupported ingredient unit: " + unitName);
        }
        FoodItem foodItem = new FoodItem(ingredientName);
        Unit unit = Unit.parseUnit(unitName);
        Amount amount = new Amount(quantity, unit);

        recipe.addIngredient(foodItem, amount);
      }
    }
    JsonNode stepsJson = requestJson.path("steps");
    if (!stepsJson.isMissingNode() && !stepsJson.isArray()) {
      return ResponseEntity.badRequest().body("steps must be an array");
    }
    if (stepsJson.isArray()) {
      for (JsonNode stepJson : stepsJson) {
        if (!stepJson.isTextual()) {
          return ResponseEntity.badRequest().body("Each step must be a string");
        }

        String stepText = stepJson.asText();

        if (stepText.isBlank()) {
          return ResponseEntity.badRequest().body("Recipe step cannot be blank");
        }

        recipe.addStep(stepText);
      }
    }
    Path filePath = Path.of(recipesPath);
    RecipeBook recipeBook;

    try {
      recipeBook = recipeFileHandler.load(filePath, user);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not load recipes");
    }
    recipeBook.addRecipe(recipe);
    try {
      recipeFileHandler.save(filePath, recipeBook);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not save recipe");
    }

    return ResponseEntity.status(HttpStatus.CREATED).body("Recipe created successfully");
  }
}
