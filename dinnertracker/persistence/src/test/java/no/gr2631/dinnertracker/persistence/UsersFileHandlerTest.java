package no.gr2631.dinnertracker.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import no.gr2631.dinnertracker.core.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the functionality of {@link UsersFileHandler}.
 *
 * <p>Verifies that users can be saved and loaded from JSON files, existing users are preserved,
 * duplicate usernames are prevented, and invalid or missing files are handled correctly.
 *
 * <p>All tests use temporary files to avoid modifying application data.
 */
class UsersFileHandlerTest {

  /** Temporary directory created by JUnit for each test. */
  @TempDir Path tempDir;

  /** File handler used throughout the tests. */
  private UsersFileHandler fileHandler;

  /** Path to the temporary JSON file. */
  private Path filePath;

  /** Initializes a new file handler and temporary file path before each test. */
  @BeforeEach
  void setUp() {
    fileHandler = new UsersFileHandler();
    filePath = tempDir.resolve("users.json");
  }

  /**
   * Tests that a user can be saved to a new JSON file.
   *
   * @throws IOException if the file cannot be written or read
   */
  @Test
  void testSaveNewUser() throws IOException {
    User user = new User("rumyana");

    fileHandler.save(filePath, user);

    assertTrue(Files.exists(filePath));

    JsonNode root = new ObjectMapper().readTree(filePath.toFile());

    assertTrue(root.isArray());
    assertEquals(1, root.size());
    assertEquals("rumyana", root.get(0).get("userName").asText());
  }

  /**
   * Tests that a previously saved user can be loaded using their username.
   *
   * @throws IOException if the file cannot be written or read
   */
  @Test
  void testLoadExistingUser() throws IOException {
    User user = new User("rumyana");

    fileHandler.save(filePath, user);

    User loadedUser = fileHandler.load(filePath, user);

    assertNotNull(loadedUser);
    assertEquals("rumyana", loadedUser.getUserName());
  }

  /**
   * Tests that loading a username not present in the file returns {@code null}.
   *
   * @throws IOException if the file cannot be written or read
   */
  @Test
  void testLoadNonExistingUser() throws IOException {
    fileHandler.save(filePath, new User("rumyana"));

    User loadedUser = fileHandler.load(filePath, new User("Camilla"));

    assertNull(loadedUser);
  }

  /**
   * Tests that loading from a file that does not exist returns {@code null}.
   *
   * @throws IOException if the file cannot be read
   */
  @Test
  void testLoadMissingFile() throws IOException {
    User loadedUser = fileHandler.load(filePath, new User("rumyana"));

    assertNull(loadedUser);
  }

  /**
   * Tests that loading from an empty file returns {@code null}.
   *
   * @throws IOException if the file cannot be created or read
   */
  @Test
  void testLoadEmptyFile() throws IOException {
    Files.createFile(filePath);

    User loadedUser = fileHandler.load(filePath, new User("rumyana"));

    assertNull(loadedUser);
  }

  /**
   * Tests that saving multiple users preserves all previously registered users in the JSON array.
   *
   * @throws IOException if the file cannot be written or read
   */
  @Test
  void testSaveMultipleUsers() throws IOException {
    fileHandler.save(filePath, new User("rumyana"));
    fileHandler.save(filePath, new User("Camilla"));
    fileHandler.save(filePath, new User("Alex"));

    JsonNode root = new ObjectMapper().readTree(filePath.toFile());

    assertEquals(3, root.size());

    assertEquals("rumyana", root.get(0).get("userName").asText());
    assertEquals("Camilla", root.get(1).get("userName").asText());
    assertEquals("Alex", root.get(2).get("userName").asText());
  }

  /**
   * Tests that saving the same username twice does not create duplicate entries in the JSON file.
   *
   * @throws IOException if the file cannot be written or read
   */
  @Test
  void testSaveDuplicateUser() throws IOException {
    fileHandler.save(filePath, new User("rumyana"));
    fileHandler.save(filePath, new User("rumyana"));

    JsonNode root = new ObjectMapper().readTree(filePath.toFile());

    assertEquals(1, root.size());
    assertEquals("rumyana", root.get(0).get("userName").asText());
  }

  /**
   * Tests that users can be loaded independently from a JSON file containing multiple registered
   * users.
   *
   * @throws IOException if the file cannot be written or read
   */
  @Test
  void testLoadSpecificUser() throws IOException {
    fileHandler.save(filePath, new User("rumyana"));
    fileHandler.save(filePath, new User("Camilla"));

    User loadedUser = fileHandler.load(filePath, new User("Camilla"));

    assertNotNull(loadedUser);
    assertEquals("Camilla", loadedUser.getUserName());
  }

  /**
   * Tests that loading from a JSON file whose root is not an array throws an {@link IOException}.
   *
   * @throws IOException if writing the test file fails
   */
  @Test
  void testLoadInvalidJsonStructure() throws IOException {
    Files.writeString(filePath, "{\"userName\":\"rumyana\"}");

    assertThrows(IOException.class, () -> fileHandler.load(filePath, new User("rumyana")));
  }

  /**
   * Tests that saving to a JSON file whose root is not an array throws an {@link IOException}.
   *
   * @throws IOException if writing the test file fails
   */
  @Test
  void testSaveInvalidJsonStructure() throws IOException {
    Files.writeString(filePath, "{\"userName\":\"rumyana\"}");

    assertThrows(IOException.class, () -> fileHandler.save(filePath, new User("Camilla")));
  }

  /**
   * Tests that saving a user to an empty file creates a valid JSON array containing that user.
   *
   * @throws IOException if the file cannot be created, written, or read
   */
  @Test
  void testSaveToEmptyFile() throws IOException {
    Files.createFile(filePath);

    fileHandler.save(filePath, new User("rumyana"));

    JsonNode root = new ObjectMapper().readTree(filePath.toFile());

    assertTrue(root.isArray());
    assertEquals(1, root.size());
  }

  /**
   * Tests that saving an already registered user does not modify the contents of the JSON file.
   *
   * @throws IOException if the file cannot be written or read
   */
  @Test
  void testDuplicateDoesNotModifyFile() throws IOException {
    fileHandler.save(filePath, new User("rumyana"));

    String originalContent = Files.readString(filePath);

    fileHandler.save(filePath, new User("rumyana"));

    String updatedContent = Files.readString(filePath);

    assertEquals(originalContent, updatedContent);
    assertFalse(updatedContent.isEmpty());
  }
}
