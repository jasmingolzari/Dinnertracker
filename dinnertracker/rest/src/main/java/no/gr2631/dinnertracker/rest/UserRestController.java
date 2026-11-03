package no.gr2631.dinnertracker.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import no.gr2631.dinnertracker.core.User;
import no.gr2631.dinnertracker.persistence.UsersFileHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for managing users. */
@RestController
public class UserRestController {

  @Value("${dinnertracker.users.path}")
  private String usersPath;

  private final UsersFileHandler usersFileHandler = new UsersFileHandler();

  /**
   * Finds a user by username.
   *
   * @param userName the username to search for
   * @return the matching user or an appropriate HTTP error
   */
  @GetMapping("/users")
  public ResponseEntity<String> getUser(
      @RequestParam(name = "userName", required = false) String userName) {

    if (userName == null || userName.isBlank()) {
      return ResponseEntity.badRequest().body("userName is required");
    }

    try {
      Path filePath = Path.of(usersPath);
      User user = usersFileHandler.load(filePath, new User(userName));

      if (user == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
      }

      return ResponseEntity.ok().body(user.getUserName());

    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not load user");
    }
  }

  /**
   * Creates and saves a new user.
   *
   * @param requestBody the JSON request containing the username
   * @return an HTTP response describing the result
   */
  @PostMapping("/users")
  public ResponseEntity<String> createUser(@RequestBody String requestBody) {

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode requestJson;

    try {
      requestJson = objectMapper.readTree(requestBody);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      return ResponseEntity.badRequest().body("Invalid JSON");
    }

    if (requestJson == null || !requestJson.isObject()) {
      return ResponseEntity.badRequest().body("JSON object is required");
    }

    JsonNode userNameNode = requestJson.path("userName");

    if (!userNameNode.isTextual() || userNameNode.asText().isBlank()) {
      return ResponseEntity.badRequest().body("Valid userName is required");
    }

    String userName = userNameNode.asText();
    User user = new User(userName);

    try {
      Path filePath = Path.of(usersPath);

      User existingUser = usersFileHandler.load(filePath, user);

      if (existingUser != null) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
      }

      usersFileHandler.save(filePath, user);

      return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");

    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not save user");
    }
  }
}
