package no.gr2631.dinnertracker.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.RecipeBook;
import no.gr2631.dinnertracker.core.User;
import no.gr2631.dinnertracker.persistence.RecipeFileHandler;

public class RestServer {

  public static Javalin createApp(Path recipesPath) {
    RecipeFileHandler recipeFileHandler = new RecipeFileHandler();
    ObjectMapper objectMapper = new ObjectMapper();

    Javalin app =
        Javalin.create(
            config -> {
              config.routes.get(
                  "/health",
                  ctx -> {
                    ctx.result("Dinnertracker REST API is running");
                  });

              config.routes.get(
                  "/recipes",
                  ctx -> {
                    String userName = ctx.queryParam("userName");
                    if (userName == null || userName.isBlank()) {
                      ctx.status(400);
                      ctx.result("userName is required");
                      return;
                    }
                    RecipeBook recipeBook = recipeFileHandler.load(recipesPath, new User(userName));

                    List<Recipe> matchingRecipes =
                        recipeBook.getRecipes().stream()
                            .filter(recipe -> recipe.getUserName().getUserName().equals(userName))
                            .toList();
                    ArrayNode recipesJson = objectMapper.createArrayNode();
                    for (Recipe recipe : matchingRecipes) {
                      ObjectNode recipeJson = recipesJson.addObject();
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
                    }

                    ctx.contentType("application/json");
                    ctx.result(recipesJson.toString());
                  });
              config.routes.post(
                  "/recipes",
                  ctx -> {
                    com.fasterxml.jackson.databind.JsonNode requestJson;

                    try {
                      requestJson = objectMapper.readTree(ctx.body());
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                      ctx.status(400);
                      ctx.result("Invalid JSON");
                      return;
                    }
                    if (requestJson == null || !requestJson.isObject()) {
                      ctx.status(400);
                      ctx.result("JSON object is required");
                      return;
                    }
                    if (!requestJson.has("userName") || !requestJson.has("name")) {
                      ctx.status(400);
                      ctx.result("userName and name are required");
                      return;
                    }
                    if (!requestJson.path("userName").isTextual()
                        || !requestJson.path("name").isTextual()) {
                      ctx.status(400);
                      ctx.result("userName and name must be strings");
                      return;
                    }

                    String userName = requestJson.path("userName").asText();
                    String recipeName = requestJson.path("name").asText();
                    if (userName.isBlank() || recipeName.isBlank()) {
                      ctx.status(400);
                      ctx.result("userName and name are required");
                      return;
                    }
                    no.gr2631.dinnertracker.core.User user =
                        new no.gr2631.dinnertracker.core.User(userName);

                    no.gr2631.dinnertracker.core.Recipe recipe =
                        new no.gr2631.dinnertracker.core.Recipe(recipeName, user);
                    com.fasterxml.jackson.databind.JsonNode ingredientsJson =
                        requestJson.path("ingredients");
                    if (!ingredientsJson.isMissingNode() && !ingredientsJson.isArray()) {
                      ctx.status(400);
                      ctx.result("ingredients must be an array");
                      return;
                    }
                    if (ingredientsJson.isArray()) {
                      for (com.fasterxml.jackson.databind.JsonNode ingredientJson :
                          ingredientsJson) {
                        if (!ingredientJson.isObject()) {
                          ctx.status(400);
                          ctx.result("Each ingredient must be a JSON object");
                          return;
                        }
                        if (!ingredientJson.has("name")
                            || !ingredientJson.has("quantity")
                            || !ingredientJson.has("unit")) {
                          ctx.status(400);
                          ctx.result("Each ingredient must have name, quantity and unit");
                          return;
                        }
                        if (!ingredientJson.path("name").isTextual()
                            || !ingredientJson.path("quantity").isNumber()
                            || !ingredientJson.path("unit").isTextual()) {
                          ctx.status(400);
                          ctx.result(
                              "Ingredient name and unit must be strings, and quantity must be a number");
                          return;
                        }
                        String ingredientName = ingredientJson.path("name").asText();

                        if (ingredientName.isBlank()) {
                          ctx.status(400);
                          ctx.result("Ingredient name cannot be blank");
                          return;
                        }
                        double quantity = ingredientJson.path("quantity").asDouble();

                        if (!Double.isFinite(quantity) || quantity <= 0) {
                          ctx.status(400);
                          ctx.result("Ingredient quantity must be a positive finite number");
                          return;
                        }
                        String unitText = ingredientJson.path("unit").asText();

                        no.gr2631.dinnertracker.core.Unit unit;

                        try {
                          unit = no.gr2631.dinnertracker.core.Unit.parseUnit(unitText);
                        } catch (IllegalArgumentException e) {
                          ctx.status(400);
                          ctx.result("Unsupported ingredient unit");
                          return;
                        }
                        no.gr2631.dinnertracker.core.FoodItem item =
                            new no.gr2631.dinnertracker.core.FoodItem(ingredientName);

                        no.gr2631.dinnertracker.core.Amount amount =
                            new no.gr2631.dinnertracker.core.Amount(quantity, unit);
                        recipe.addIngredient(item, amount);
                      }
                    }
                    com.fasterxml.jackson.databind.JsonNode stepsJson = requestJson.path("steps");
                    if (!stepsJson.isMissingNode() && !stepsJson.isArray()) {
                      ctx.status(400);
                      ctx.result("steps must be an array");
                      return;
                    }
                    if (stepsJson.isArray()) {
                      for (com.fasterxml.jackson.databind.JsonNode stepJson : stepsJson) {
                        if (!stepJson.isTextual()) {
                          ctx.status(400);
                          ctx.result("Each step must be a string");
                          return;
                        }
                        String stepText = stepJson.asText();

                        if (stepText.isBlank()) {
                          ctx.status(400);
                          ctx.result("Recipe step cannot be blank");
                          return;
                        }
                        recipe.addStep(stepText);
                      }
                    }
                    no.gr2631.dinnertracker.core.RecipeBook recipeBook =
                        recipeFileHandler.load(recipesPath, user);
                    recipeBook.addRecipe(recipe);
                    recipeFileHandler.save(recipesPath, recipeBook);

                    ctx.status(201);
                    ctx.result("User: " + userName + ", Recipe: " + recipeName);
                  });
            });

    return app;
  }

  public static void main(String[] args) {
    Path recipesPath =
        Path.of(
            System.getProperty("dinnertracker.recipes.path", "dinnertracker/data/recipes.json"));
    Javalin app = createApp(recipesPath);
    app.start(7070);
  }
}
