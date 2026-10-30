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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecipeRestControllerTest {

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void setUp() throws Exception {
    Path testFile = Path.of("target/recipes-spring-automated-test.json");

    Files.createDirectories(testFile.getParent());
    Files.writeString(testFile, "[]");
  }

  @Test
  void testHealthEndpoint() throws Exception {
    mockMvc
        .perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(content().string("Dinnertracker REST API is running"));
  }

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

  @Test
  void testGetRecipesWithoutUserNameReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/recipes")).andExpect(status().isBadRequest());
  }

  @Test
  void testGetRecipesWithEmptyUserNameReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/recipes").param("userName", "")).andExpect(status().isBadRequest());
  }

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
        .perform(post("/recipes").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(content().string("Recipe created successfully"));

    Path testFile = Path.of("target/recipes-spring-automated-test.json");

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertTrue(savedData.contains("\"userName\""));

    org.junit.jupiter.api.Assertions.assertTrue(savedData.contains("Testbruker"));

    org.junit.jupiter.api.Assertions.assertTrue(savedData.contains("Testmiddag"));

    org.junit.jupiter.api.Assertions.assertTrue(savedData.contains("Lag middagen"));
  }

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
        .perform(post("/recipes").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest());

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }

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
        .perform(post("/recipes").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest());

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }

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
        .perform(post("/recipes").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest());

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }

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
        .perform(post("/recipes").contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andExpect(status().isBadRequest());

    String savedData = Files.readString(testFile);

    org.junit.jupiter.api.Assertions.assertEquals(originalData, savedData);
  }
}
