 package no.gr2631.dinnertracker.rest;
 import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.User;
import no.gr2631.dinnertracker.persistence.RecipeFileHandler;
import java.nio.file.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecipeRestController {
    private final ObjectMapper objectMapper = new ObjectMapper();
        @Value("${dinnertracker.recipes.path}")
    private String recipesPath;

    private final RecipeFileHandler recipeFileHandler = new RecipeFileHandler();
    @GetMapping("/health")
    public String health() {
        return "Dinnertracker REST API is running";
    }
        @GetMapping(value = "/recipes", produces = MediaType.APPLICATION_JSON_VALUE)
public String getRecipes(@RequestParam(name = "userName", required = false) String userName) {
        if (userName == null || userName.isBlank()) {
    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST, "userName is required");
}       Path filePath = Path.of(recipesPath);
        System.out.println("Spring Boot working directory: " + System.getProperty("user.dir"));
RecipeBook recipeBook;

try {
    recipeBook = recipeFileHandler.load(filePath, new User(userName));
    List<Recipe> recipes = recipeBook.getRecipes();
    ArrayNode recipesJson = objectMapper.createArrayNode();
    for (Recipe recipe : recipes) {
        if (!recipe.getUserName().getUserName().equals(userName)) {
    continue;
}
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
recipesJson.add(recipeJson);


}
return recipesJson.toString();
} catch (IOException e) {
    throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Could not load recipes",
        e);
}

    }
    @PostMapping("/recipes")
public ResponseEntity<String> createRecipe(@RequestBody String requestBody) {
    JsonNode requestJson;

    try {
        requestJson = objectMapper.readTree(requestBody);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
        return ResponseEntity.badRequest().body("Invalid JSON");
    }
    if (requestJson == null || !requestJson.isObject()) {
    return ResponseEntity.badRequest()
            .body("JSON object is required");
}
if (!requestJson.has("userName") || !requestJson.has("name")) {
    return ResponseEntity.badRequest()
            .body("userName and name are required");
}
if (!requestJson.path("userName").isTextual()
        || !requestJson.path("name").isTextual()) {
    return ResponseEntity.badRequest()
            .body("userName and name must be strings");
}
String userName = requestJson.path("userName").asText();
String recipeName = requestJson.path("name").asText();

if (userName.isBlank() || recipeName.isBlank()) {
    return ResponseEntity.badRequest()
            .body("userName and name are required");
}
User user = new User(userName);
Recipe recipe = new Recipe(recipeName, user);
JsonNode ingredientsJson = requestJson.path("ingredients");

if (!ingredientsJson.isMissingNode() && !ingredientsJson.isArray()) {
    return ResponseEntity.badRequest()
            .body("ingredients must be an array");
}
if (ingredientsJson.isArray()) {
    for (JsonNode ingredientJson : ingredientsJson) {
        if (ingredientJson == null || !ingredientJson.isObject()) {
    return ResponseEntity.badRequest()
            .body("Each ingredient must be an object");
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
    return ResponseEntity.badRequest()
            .body("Invalid ingredient field types");
}
String ingredientName = ingredientJson.path("name").asText();

if (ingredientName.isBlank()) {
    return ResponseEntity.badRequest()
            .body("Ingredient name must not be blank");
}
double quantity = ingredientJson.path("quantity").asDouble();

if (!Double.isFinite(quantity) || quantity <= 0) {
    return ResponseEntity.badRequest()
            .body("Ingredient quantity must be greater than zero");
}
String unitName = ingredientJson.path("unit").asText();

if (unitName.isBlank()) {
    return ResponseEntity.badRequest()
            .body("Ingredient unit must not be blank");
}
try {
    Unit.parseUnit(unitName);
} catch (IllegalArgumentException e) {
    return ResponseEntity.badRequest()
            .body("Unsupported ingredient unit: " + unitName);
}
FoodItem foodItem = new FoodItem(ingredientName);
Unit unit = Unit.parseUnit(unitName);
Amount amount = new Amount(quantity, unit);

recipe.addIngredient(foodItem, amount);
    }
}
JsonNode stepsJson = requestJson.path("steps");
if (!stepsJson.isMissingNode() && !stepsJson.isArray()) {
    return ResponseEntity.badRequest()
            .body("steps must be an array");
}
if (stepsJson.isArray()) {
    for (JsonNode stepJson : stepsJson) {
        if (!stepJson.isTextual()) {
            return ResponseEntity.badRequest()
                    .body("Each step must be a string");
        }

        String stepText = stepJson.asText();

        if (stepText.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Recipe step cannot be blank");
        }

        recipe.addStep(stepText);
    }
}
Path filePath = Path.of(recipesPath);
RecipeBook recipeBook;

try {
    recipeBook = recipeFileHandler.load(filePath, user);
} catch (IOException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Could not load recipes");
}
recipeBook.addRecipe(recipe);
try {
    recipeFileHandler.save(filePath, recipeBook);
} catch (IOException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Could not save recipe");
}

    return ResponseEntity.status(HttpStatus.CREATED)
        .body("Recipe created successfully");
}
}
