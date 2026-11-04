package no.gr2631.dinnertracker.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

/** Tests for the user REST API. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserRestControllerTest {

  @Autowired private MockMvc mockMvc;

  private Path testFile;

  @BeforeEach
  @SuppressWarnings("unused")
  void setUp() throws Exception {
    testFile = Path.of("target/users-spring-automated-test.json");

    Files.createDirectories(testFile.getParent());
    Files.writeString(testFile, "[]");
  }

  @Test
  void testCreateUserSuccessfully() throws Exception {

    String requestBody =
        """
            {
                "userName": "Testbruker"
            }
            """;

    mockMvc
        .perform(post("/users").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(content().string("User created successfully"));

    String savedData = Files.readString(testFile);

    assertTrue(savedData.contains("\"userName\" : \"Testbruker\""));
  }

  @Test
  void testGetExistingUserSuccessfully() throws Exception {

    Files.writeString(
        testFile,
        """
            [
                {
                    "userName": "Testbruker"
                }
            ]
            """);

    mockMvc
        .perform(get("/users").param("userName", "Testbruker"))
        .andExpect(status().isOk())
        .andExpect(content().string("Testbruker"));
  }

  @Test
  void testGetNonExistingUserReturnsNotFound() throws Exception {

    mockMvc
        .perform(get("/users").param("userName", "UkjentBruker"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("User not found"));
  }

  @Test
  void testCreateUserWithBlankNameReturnsBadRequest() throws Exception {

    String requestBody =
        """
            {
                "userName": ""
            }
            """;

    mockMvc
        .perform(post("/users").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Valid userName is required"));
  }

  @Test
  void testCreateDuplicateUserReturnsConflict() throws Exception {

    Files.writeString(
        testFile,
        """
            [
                {
                    "userName": "Testbruker"
                }
            ]
            """);

    String requestBody =
        """
            {
                "userName": "Testbruker"
            }
            """;

    mockMvc
        .perform(post("/users").contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
        .andExpect(status().isConflict())
        .andExpect(content().string("User already exists"));
  }
}
