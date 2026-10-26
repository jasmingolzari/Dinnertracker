package no.gr2631.dinnertracker.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RestServerTest {

  @Test
  void testJUnitWorks() {
    org.junit.jupiter.api.Assertions.assertEquals(2, 1 + 1);
  }

  @Test
  void testHealthEndpoint() throws Exception {
    Path recipesPath = Path.of("dinnertracker/data/recipes-rest-test.json");
    Javalin app = RestServer.createApp(recipesPath);
    app.start(0);
    try {
      HttpClient client = HttpClient.newHttpClient();
      int port = app.port();
      String healthUrl = "http://localhost:" + port + "/health";
      HttpRequest request =
          HttpRequest.newBuilder().uri(java.net.URI.create(healthUrl)).GET().build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      org.junit.jupiter.api.Assertions.assertEquals(200, response.statusCode());
      org.junit.jupiter.api.Assertions.assertEquals(
          "Dinnertracker REST API is running", response.body());
    } finally {
      app.stop();
    }
  }

  @Test
  void testGetRecipesEndpoint() throws Exception {
    Path recipesPath =
        Path.of(
            java.util.Objects.requireNonNull(
                    RestServerTest.class.getResource("/recipes-rest-test.json"))
                .toURI());
    org.junit.jupiter.api.Assertions.assertTrue(java.nio.file.Files.exists(recipesPath));

    Javalin app = RestServer.createApp(recipesPath);

    app.start(0);

    try {
      HttpClient client = HttpClient.newHttpClient();
      int port = app.port();
      String recipesUrl = "http://localhost:" + port + "/recipes?userName=Rumyana";
      HttpRequest request =
          HttpRequest.newBuilder().uri(java.net.URI.create(recipesUrl)).GET().build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      org.junit.jupiter.api.Assertions.assertEquals(200, response.statusCode());
      String responseBody = response.body();
      ObjectMapper mapper = new ObjectMapper();
      com.fasterxml.jackson.databind.JsonNode recipesJson = mapper.readTree(responseBody);
      org.junit.jupiter.api.Assertions.assertTrue(recipesJson.isArray());
      org.junit.jupiter.api.Assertions.assertTrue(recipesJson.size() > 0);

      for (com.fasterxml.jackson.databind.JsonNode recipe : recipesJson) {
        org.junit.jupiter.api.Assertions.assertEquals("Rumyana", recipe.path("userName").asText());
        org.junit.jupiter.api.Assertions.assertTrue(
            java.util.stream.StreamSupport.stream(recipesJson.spliterator(), false)
                .anyMatch(item -> "Pannekaker".equals(item.path("name").asText())));
      }

    } finally {
      app.stop();
    }
  }

  @Test
  void testPostRecipesEndpoint(@TempDir Path tempDir) throws Exception {
    Path recipesPath = tempDir.resolve("recipes-post-test.json");
    java.nio.file.Files.writeString(recipesPath, "[]");
    Javalin app = RestServer.createApp(recipesPath);
    app.start(0);
    try {
      HttpClient client = HttpClient.newHttpClient();
      int port = app.port();
      String recipesUrl = "http://localhost:" + port + "/recipes";
      String recipeJson = "{\"userName\":\"Testbruker\",\"name\":\"Testpannekaker\"}";
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(java.net.URI.create(recipesUrl))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(recipeJson))
              .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      org.junit.jupiter.api.Assertions.assertEquals(201, response.statusCode());
      org.junit.jupiter.api.Assertions.assertEquals(
          "User: Testbruker, Recipe: Testpannekaker", response.body());
      String savedJson = java.nio.file.Files.readString(recipesPath);
      org.junit.jupiter.api.Assertions.assertTrue(savedJson.contains("Testpannekaker"));
      org.junit.jupiter.api.Assertions.assertTrue(savedJson.contains("Testbruker"));
      ObjectMapper mapper = new ObjectMapper();
      com.fasterxml.jackson.databind.JsonNode savedRecipes = mapper.readTree(savedJson);
      org.junit.jupiter.api.Assertions.assertTrue(savedRecipes.isArray());
      org.junit.jupiter.api.Assertions.assertTrue(savedRecipes.size() > 0);
      com.fasterxml.jackson.databind.JsonNode savedRecipe = savedRecipes.get(0);
      org.junit.jupiter.api.Assertions.assertEquals(
          "Testpannekaker", savedRecipe.path("name").asText());
      org.junit.jupiter.api.Assertions.assertEquals(
          "Testbruker", savedRecipe.path("userName").asText());

    } finally {
      app.stop();
    }
  }

  @Test
  void testPostRecipesMissingName(@TempDir Path tempDir) throws Exception {
    Path recipesPath = tempDir.resolve("recipes-missing-name-test.json");
    java.nio.file.Files.writeString(recipesPath, "[]");
    Javalin app = RestServer.createApp(recipesPath);
    app.start(0);
    try {
      HttpClient client = HttpClient.newHttpClient();
      int port = app.port();
      String recipesUrl = "http://localhost:" + port + "/recipes";
      String recipeJson = "{\"userName\":\"Testbruker\"}";
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(java.net.URI.create(recipesUrl))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(recipeJson))
              .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      org.junit.jupiter.api.Assertions.assertEquals(400, response.statusCode());
      org.junit.jupiter.api.Assertions.assertEquals(
          "userName and name are required", response.body());

    } finally {
      app.stop();
    }
  }

  @Test
  void testPostRecipesMissingUserName(@TempDir Path tempDir) throws Exception {
    Path recipesPath = tempDir.resolve("recipes-missing-username-test.json");
    java.nio.file.Files.writeString(recipesPath, "[]");
    Javalin app = RestServer.createApp(recipesPath);
    app.start(0);
    try {
      HttpClient client = HttpClient.newHttpClient();
      int port = app.port();
      String recipesUrl = "http://localhost:" + port + "/recipes";
      String recipeJson = "{\"name\":\"Testpannekaker\"}";
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(java.net.URI.create(recipesUrl))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(recipeJson))
              .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      org.junit.jupiter.api.Assertions.assertEquals(400, response.statusCode());
      org.junit.jupiter.api.Assertions.assertEquals(
          "userName and name are required", response.body());
      String savedJson = java.nio.file.Files.readString(recipesPath);
      org.junit.jupiter.api.Assertions.assertEquals("[]", savedJson.trim());

    } finally {
      app.stop();
    }
  }
}
