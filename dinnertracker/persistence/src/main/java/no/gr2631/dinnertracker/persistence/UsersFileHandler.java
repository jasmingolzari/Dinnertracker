package no.gr2631.dinnertracker.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import no.gr2631.dinnertracker.core.User;

/**
 * Handles reading and writing users to a JSON file. All users are stored together in a JSON array,
 * where each user is represented by an object containing their username.
 *
 * <p>Existing users are preserved when new users are saved, and duplicate usernames are not added.
 */
public class UsersFileHandler {

  private final ObjectMapper objectMapper;

  /**
   * Constructs a new UsersFileHandler. Initializes the Jackson ObjectMapper and enables indentation
   * for readable JSON output.
   */
  public UsersFileHandler() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  /**
   * Loads a specific user from the given JSON file. Searches the stored users for a username
   * matching the provided user.
   *
   * <p>If the file does not exist, is empty, or the user cannot be found, {@code null} is returned.
   *
   * @param path the path to the JSON file to read from
   * @param user the user whose username should be searched for
   * @return the matching user, or {@code null} if no match exists
   * @throws IOException if the file cannot be read or contains invalid JSON
   */
  public User load(Path path, User user) throws IOException {
    if (!Files.exists(path) || Files.size(path) == 0) {
      return null;
    }

    JsonNode root = objectMapper.readTree(path.toFile());

    if (root == null || !root.isArray()) {
      throw new IOException("Expected a JSON array of users");
    }

    for (JsonNode userNode : root) {
      String storedUsername = userNode.path("userName").asText();

      if (storedUsername.equals(user.getUserName())) {
        return new User(storedUsername);
      }
    }

    return null;
  }

  /**
   * Saves a user to the given JSON file without removing existing users. Reads the existing user
   * array, checks whether the username is already registered, and adds the new user if no match
   * exists.
   *
   * <p>If the username already exists, the file is left unchanged. If the file does not exist or is
   * empty, a new JSON array is created.
   *
   * @param path the path to the JSON file to write to
   * @param user the user to save
   * @throws IOException if the file cannot be read or written, or contains invalid JSON
   */
  public void save(Path path, User user) throws IOException {
    ArrayNode usersList = objectMapper.createArrayNode();

    if (Files.exists(path) && Files.size(path) > 0) {
      JsonNode root = objectMapper.readTree(path.toFile());

      if (root == null || !root.isArray()) {
        throw new IOException("Expected a JSON array of users");
      }

      for (JsonNode userNode : root) {
        usersList.add(userNode);
      }
    }

    for (JsonNode userNode : usersList) {
      if (user.getUserName().equals(userNode.path("userName").asText())) {
        return;
      }
    }

    ObjectNode newUserNode = objectMapper.createObjectNode();
    newUserNode.put("userName", user.getUserName());

    usersList.add(newUserNode);

    objectMapper.writeValue(path.toFile(), usersList);
  }
}
