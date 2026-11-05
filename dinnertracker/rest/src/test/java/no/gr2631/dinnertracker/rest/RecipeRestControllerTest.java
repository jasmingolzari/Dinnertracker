package no.gr2631.dinnertracker.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for the recipe REST endpoints. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecipeRestControllerTest {

  @Autowired private MockMvc mockMvc;

  /** Resets the test recipe file before each test. */
  @BeforeEach
  void setUp() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");

    Files.createDirectories(testFile.getParent());
    Files.writeString(testFile, "[]");
  }

  /** Verifies that the health endpoint responds successfully. */
  @Test
  void testHealthEndpoint() throws Exception {
    mockMvc
        .perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(content().string("Dinnertracker REST API is running"));
  }

  /** Verifies that recipes are filtered by user name. */
  @Test
  void testGetRecipesEndpoint() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");

    String testData =
        """
        [
          {
            "userName": "Testbruker",
            "name": "Testpannekaker",
            "ingredients": [],
            "steps": []
          },
          {
            "userName": "AnnenBruker",
            "name": "Testpizza",
            "ingredients": [],
            "steps": []
          }
        ]
        """;

    Files.writeString(testFile, testData);

    mockMvc
        .perform(get("/recipes").param("userName", "Testbruker"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(jsonPath("$[0].userName").value("Testbruker"))
        .andExpect(jsonPath("$[0].name").value("Testpannekaker"))
        .andExpect(jsonPath("$.length()").value(1));
  }

  /** Verifies that all recipes are returned, from every user, with their owners kept. */
  @Test
  void testGetAllRecipesEndpoint() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");

    String testData =
        """
        [
          {
            "userName": "Testbruker",
            "name": "Testpannekaker",
            "ingredients": [{"name": "Mel", "quantity": 200, "unit": "GRAM"}],
            "steps": ["Bland"]
          },
          {
            "userName": "AnnenBruker",
            "name": "Testpizza",
            "ingredients": [],
            "steps": []
          }
        ]
        """;

    Files.writeString(testFile, testData);

    mockMvc
        .perform(get("/recipes/all"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].userName").value("Testbruker"))
        .andExpect(jsonPath("$[0].name").value("Testpannekaker"))
        .andExpect(jsonPath("$[0].ingredients[0].name").value("Mel"))
        .andExpect(jsonPath("$[0].steps[0]").value("Bland"))
        .andExpect(jsonPath("$[1].userName").value("AnnenBruker"))
        .andExpect(jsonPath("$[1].name").value("Testpizza"));
  }

  /** Verifies that an empty recipe file gives an empty list of all recipes. */
  @Test
  void testGetAllRecipesWithNoRecipesReturnsEmptyList() throws Exception {
    mockMvc
        .perform(get("/recipes/all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  /** Verifies that a missing user name is rejected. */
  @Test
  void testGetRecipesWithoutUserNameReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/recipes")).andExpect(status().isBadRequest());
  }

  /** Verifies that an empty user name is rejected. */
  @Test
  void testGetRecipesWithEmptyUserNameReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/recipes").param("userName", "")).andExpect(status().isBadRequest());
  }

  /** Verifies that a valid recipe can be created. */
  @Test
  void testCreateRecipeSuccessfully() throws Exception {
    String requestBody =
        """
        {
          "userName": "Testbruker",
          "name": "Testmiddag",
          "ingredients": [],
          "steps": ["Lag middagen"]
        }
        """;

    mockMvc
        .perform(
            post("/recipes").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(content().string("Recipe created successfully"));

    Path testFile = Path.of("target/recipes-spring-automated-test.json");

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertTrue(savedData.contains("\"userName\""));

    org.junit.jupiter.api.Assertions.assertTrue(savedData.contains("Testbruker"));

    org.junit.jupiter.api.Assertions.assertTrue(savedData.contains("Testmiddag"));

    org.junit.jupiter.api.Assertions.assertTrue(savedData.contains("Lag middagen"));
  }

  /** Verifies that a recipe without a name is rejected. */
  @Test
  void testCreateRecipeWithoutNameReturnsBadRequest() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");

    String originalData = Files.readString(testFile);

    String requestBody =
        """
        {
          "userName": "Testbruker",
          "ingredients": [],
          "steps": ["Lag middagen"]
        }
        """;

    mockMvc
        .perform(
            post("/recipes").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isBadRequest());

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }

  /** Verifies that a recipe with a blank step is rejected. */
  @Test
  void testCreateRecipeWithBlankStepReturnsBadRequest() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");

    String originalData = Files.readString(testFile);

    String requestBody =
        """
        {
          "userName": "Testbruker",
          "name": "Ugyldig middag",
          "ingredients": [],
          "steps": [""]
        }
        """;

    mockMvc
        .perform(
            post("/recipes").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isBadRequest());

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }

  /** Verifies that a recipe without a user name is rejected. */
  @Test
  void testCreateRecipeWithoutUserNameReturnsBadRequest() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");

    String originalData = Files.readString(testFile);

    String requestBody =
        """
        {
          "name": "Testmiddag",
          "ingredients": [],
          "steps": ["Lag middagen"]
        }
        """;

    mockMvc
        .perform(
            post("/recipes").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isBadRequest());

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }

  /** Verifies that a recipe with an empty user name is rejected. */
  @Test
  void testCreateRecipeWithEmptyUserNameReturnsBadRequest() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");

    String originalData = Files.readString(testFile);

    String requestBody =
        """
        {
          "userName": "",
          "name": "Testmiddag",
          "ingredients": [],
          "steps": ["Lag middagen"]
        }
        """;

    mockMvc
        .perform(
            post("/recipes").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isBadRequest());

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }

  /** Verifies that an ingredient without a quantity is rejected. */
  @Test
  void testCreateRecipeWithMissingIngredientQuantityReturnsBadRequest() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");
    String originalData = Files.readString(testFile);

    String requestBody =
        """
            {
                "userName": "Testbruker",
                "name": "Ugyldig oppskrift",
                "ingredients": [
                    {
                        "name": "Mel",
                        "unit": "GRAM"
                    }
                ],
                "steps": ["Bland ingrediensene"]
            }
            """;

    mockMvc
        .perform(
            post("/recipes").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Ingredient name, quantity and unit are required"));

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }

  /** Verifies that a non-numeric ingredient quantity is rejected. */
  @Test
  void testCreateRecipeWithInvalidIngredientQuantityTypeReturnsBadRequest() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");
    String originalData = Files.readString(testFile);

    String requestBody =
        """
            {
                "userName": "Testbruker",
                "name": "Ugyldig oppskrift",
                "ingredients": [
                    {
                        "name": "Mel",
                        "quantity": "fem",
                        "unit": "GRAM"
                    }
                ],
                "steps": ["Bland ingrediensene"]
            }
            """;

    mockMvc
        .perform(
            post("/recipes").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid ingredient field types"));

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }

  /** Verifies that a recipe with a valid ingredient can be created. */
  @Test
  void testCreateRecipeWithValidIngredientSuccessfully() throws Exception {
    String requestBody =
        """
            {
                "userName": "Testbruker",
                "name": "Testpannekaker",
                "ingredients": [
                    {
                        "name": "Mel",
                        "quantity": 200,
                        "unit": "GRAM"
                    }
                ],
                "steps": ["Bland ingrediensene"]
            }
            """;

    mockMvc
        .perform(
            post("/recipes").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(content().string("Recipe created successfully"));

    mockMvc
        .perform(get("/recipes").param("userName", "Testbruker"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Testpannekaker"))
        .andExpect(jsonPath("$[0].ingredients[0].name").value("Mel"))
        .andExpect(jsonPath("$[0].ingredients[0].quantity").value(200))
        .andExpect(jsonPath("$[0].ingredients[0].unit").value("GRAM"))
        .andExpect(jsonPath("$[0].steps[0]").value("Bland ingrediensene"));
  }

  /** Verifies that a JSON array request is rejected. */
  @Test
  void testCreateRecipeWithJsonArrayReturnsBadRequest() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");
    String originalData = Files.readString(testFile);

    mockMvc
        .perform(post("/recipes").contentType(MediaType.APPLICATION_JSON_VALUE).content("[]"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("JSON object is required"));

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }

  /** Verifies that a non-text recipe name is rejected. */
  @Test
  void testCreateRecipeWithNumericNameReturnsBadRequest() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");
    String originalData = Files.readString(testFile);

    String requestBody =
        """
            {
                "userName": "Testbruker",
                "name": 123,
                "ingredients": [],
                "steps": ["Bland ingrediensene"]
            }
            """;

    mockMvc
        .perform(
            post("/recipes").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("userName and name must be strings"));

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }

  /**
   * Posts a recipe that should be rejected, and checks the error message and that nothing was
   * saved.
   *
   * @param requestBody the JSON sent to the server
   * @param expectedMessage the error message the server should answer with
   */
  private void assertRecipeRejected(String requestBody, String expectedMessage) throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");
    String originalData = Files.readString(testFile);

    mockMvc
        .perform(
            post("/recipes").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(expectedMessage));

    org.junit.jupiter.api.Assertions.assertEquals(originalData, Files.readString(testFile));
  }

  /** Verifies that a body that is not valid JSON is rejected. */
  @Test
  void testCreateRecipeWithInvalidJsonReturnsBadRequest() throws Exception {
    assertRecipeRejected("{", "Invalid JSON");
  }

  /** Verifies that a blank recipe name is rejected. */
  @Test
  void testCreateRecipeWithBlankNameReturnsBadRequest() throws Exception {
    assertRecipeRejected(
        "{\"userName\": \"Testbruker\", \"name\": \" \"}", "userName and name are required");
  }

  /** Verifies that ingredients must be sent as an array. */
  @Test
  void testCreateRecipeWithIngredientsNotArrayReturnsBadRequest() throws Exception {
    assertRecipeRejected(
        "{\"userName\": \"Testbruker\", \"name\": \"Suppe\", \"ingredients\": \"Mel\"}",
        "ingredients must be an array");
  }

  /** Verifies that each ingredient must be a JSON object. */
  @Test
  void testCreateRecipeWithIngredientNotObjectReturnsBadRequest() throws Exception {
    assertRecipeRejected(
        "{\"userName\": \"Testbruker\", \"name\": \"Suppe\", \"ingredients\": [\"Mel\"]}",
        "Each ingredient must be an object");
  }

  /** Verifies that an ingredient with a blank name is rejected. */
  @Test
  void testCreateRecipeWithBlankIngredientNameReturnsBadRequest() throws Exception {
    assertRecipeRejected(
        recipeWithIngredient("{\"name\": \" \", \"quantity\": 1, \"unit\": \"GRAM\"}"),
        "Ingredient name must not be blank");
  }

  /** Verifies that an ingredient quantity of zero is rejected. */
  @Test
  void testCreateRecipeWithZeroIngredientQuantityReturnsBadRequest() throws Exception {
    assertRecipeRejected(
        recipeWithIngredient("{\"name\": \"Mel\", \"quantity\": 0, \"unit\": \"GRAM\"}"),
        "Ingredient quantity must be greater than zero");
  }

  /** Verifies that an ingredient with a blank unit is rejected. */
  @Test
  void testCreateRecipeWithBlankIngredientUnitReturnsBadRequest() throws Exception {
    assertRecipeRejected(
        recipeWithIngredient("{\"name\": \"Mel\", \"quantity\": 1, \"unit\": \" \"}"),
        "Ingredient unit must not be blank");
  }

  /** Verifies that an unknown unit is rejected. */
  @Test
  void testCreateRecipeWithUnknownIngredientUnitReturnsBadRequest() throws Exception {
    assertRecipeRejected(
        recipeWithIngredient("{\"name\": \"Mel\", \"quantity\": 1, \"unit\": \"kopp\"}"),
        "Unsupported ingredient unit: kopp");
  }

  /** Verifies that steps must be sent as an array. */
  @Test
  void testCreateRecipeWithStepsNotArrayReturnsBadRequest() throws Exception {
    assertRecipeRejected(
        "{\"userName\": \"Testbruker\", \"name\": \"Suppe\", \"steps\": \"Kok\"}",
        "steps must be an array");
  }

  /** Verifies that each step must be text. */
  @Test
  void testCreateRecipeWithNonTextStepReturnsBadRequest() throws Exception {
    assertRecipeRejected(
        "{\"userName\": \"Testbruker\", \"name\": \"Suppe\", \"steps\": [1]}",
        "Each step must be a string");
  }

  /** Verifies that an unreadable recipe file gives a server error for every recipe endpoint. */
  @Test
  void testUnreadableRecipeFileReturnsServerError() throws Exception {
    Files.writeString(Path.of("target/recipes-spring-automated-test.json"), "[");

    mockMvc.perform(get("/recipes/all")).andExpect(status().isInternalServerError());
    mockMvc
        .perform(get("/recipes").param("userName", "Testbruker"))
        .andExpect(status().isInternalServerError());
    mockMvc
        .perform(
            post("/recipes")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"userName\": \"Testbruker\", \"name\": \"Suppe\"}"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Could not load recipes"));
  }

  /**
   * Builds a recipe request with one ingredient.
   *
   * @param ingredientJson the ingredient as a JSON object
   * @return the full recipe request
   */
  private static String recipeWithIngredient(String ingredientJson) {
    return "{\"userName\": \"Testbruker\", \"name\": \"Suppe\", \"ingredients\": ["
        + ingredientJson
        + "]}";
  }
}
