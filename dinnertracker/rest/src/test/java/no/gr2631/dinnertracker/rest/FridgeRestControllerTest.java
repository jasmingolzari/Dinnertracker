package no.gr2631.dinnertracker.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

/** Tests for the fridge REST controller. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FridgeRestControllerTest {

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void setUp() throws Exception {
    Path testFile = Path.of("target/fridges-spring-automated-test.json");
    Files.createDirectories(testFile.getParent());
    Files.writeString(testFile, "[]");
  }

  @Test
  void testGetEmptyFridgeSuccessfully() throws Exception {
    mockMvc
        .perform(get("/fridges").param("userName", "Testbruker"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userName").value("Testbruker"))
        .andExpect(jsonPath("$.foodItems.length()").value(0));
  }

  @Test
  void testGetFridgeWithoutUserNameReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/fridges")).andExpect(status().isBadRequest());
  }

  @Test
  void testUpdateFridgeSuccessfully() throws Exception {
    String requestBody =
        """
        {
          "foodItems": [
            {
              "name": "Melk",
              "quantity": 2,
              "unit": "L"
            }
          ]
        }
                """;

    mockMvc
        .perform(
            put("/fridges")
                .param("userName", "Testbruker")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/fridges").param("userName", "Testbruker"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userName").value("Testbruker"))
        .andExpect(jsonPath("$.foodItems.length()").value(1))
        .andExpect(jsonPath("$.foodItems[0].name").value("Melk"))
        .andExpect(jsonPath("$.foodItems[0].quantity").value(2))
        .andExpect(jsonPath("$.foodItems[0].unit").value("LITRE"));
  }

  @Test
  void testUpdateFridgeWithNegativeQuantityReturnsBadRequest() throws Exception {
    String requestBody =
        """
        {
          "foodItems": [
            {
              "name": "Melk",
              "quantity": -2,
              "unit": "L"
            }
          ]
        }
        """;

    mockMvc
        .perform(
            put("/fridges")
                .param("userName", "Testbruker")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testInvalidUpdateDoesNotOverwriteExistingFridge() throws Exception {
    String validRequest =
        """
        {
          "foodItems": [
            {
              "name": "Melk",
              "quantity": 2,
              "unit": "L"
            }
          ]
        }
        """;

    String invalidRequest =
        """
        {
          "foodItems": [
            {
              "name": "Brød",
              "quantity": -3,
              "unit": "KG"
            }
          ]
        }
        """;

    mockMvc
        .perform(
            put("/fridges")
                .param("userName", "Testbruker")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            put("/fridges")
                .param("userName", "Testbruker")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(get("/fridges").param("userName", "Testbruker"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.foodItems.length()").value(1))
        .andExpect(jsonPath("$.foodItems[0].name").value("Melk"))
        .andExpect(jsonPath("$.foodItems[0].quantity").value(2))
        .andExpect(jsonPath("$.foodItems[0].unit").value("LITRE"));
  }

  @Test
  void testUpdatingOneFridgePreservesAnotherUsersFridge() throws Exception {
    String firstFridge =
        """
        {
          "foodItems": [
            {
              "name": "Melk",
              "quantity": 2,
              "unit": "L"
            }
          ]
        }
        """;

    String secondFridge =
        """
        {
          "foodItems": [
            {
              "name": "Brød",
              "quantity": 1,
              "unit": "KG"
            }
          ]
        }
        """;

    mockMvc
        .perform(
            put("/fridges")
                .param("userName", "Bruker1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstFridge))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            put("/fridges")
                .param("userName", "Bruker2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondFridge))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/fridges").param("userName", "Bruker1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.foodItems.length()").value(1))
        .andExpect(jsonPath("$.foodItems[0].name").value("Melk"))
        .andExpect(jsonPath("$.foodItems[0].quantity").value(2))
        .andExpect(jsonPath("$.foodItems[0].unit").value("LITRE"));

    mockMvc
        .perform(get("/fridges").param("userName", "Bruker2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.foodItems.length()").value(1))
        .andExpect(jsonPath("$.foodItems[0].name").value("Brød"))
        .andExpect(jsonPath("$.foodItems[0].quantity").value(1));
  }

  @Test
  void testUpdateFridgeWithBlankUserNameReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            put("/fridges")
                .param("userName", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"foodItems\":[]}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testUpdateFridgeWithInvalidJsonReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            put("/fridges")
                .param("userName", "Testbruker")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
        .andExpect(status().isBadRequest());
  }
}
