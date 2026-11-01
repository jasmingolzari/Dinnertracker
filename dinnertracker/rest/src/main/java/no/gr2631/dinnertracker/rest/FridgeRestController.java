package no.gr2631.dinnertracker.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.User;
import no.gr2631.dinnertracker.persistence.FridgeFileHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


/**
 * REST controller for managing user fridges.
 */
@RestController
public class FridgeRestController {

  @Value("${dinnertracker.fridges.path}")
  private String fridgesPath;

  private final FridgeFileHandler fridgeFileHandler = new FridgeFileHandler();

  private final ObjectMapper objectMapper = new ObjectMapper();


  /**
   * Returns the fridge belonging to the specified user.
   *
   * @param userName the username of the fridge owner
   * @return the fridge contents as JSON
   */


  @GetMapping(value = "/fridges", produces = MediaType.APPLICATION_JSON_VALUE)
  public String getFridge(@RequestParam(name = "userName") String userName) {
    if (userName.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "userName is required");
    }



    try {
      Fridge fridge = fridgeFileHandler.load(
          Path.of(fridgesPath), new User(userName));

      ObjectNode fridgeJson = objectMapper.createObjectNode();
      fridgeJson.put("userName", userName);

      ArrayNode foodItemsJson = fridgeJson.putArray("foodItems");

      for (Map.Entry<FoodItem, Amount> entry : fridge.getFoodItems().entrySet()) {
        ObjectNode foodItemJson = foodItemsJson.addObject();
        foodItemJson.put("name", entry.getKey().getName());
        foodItemJson.put("quantity", entry.getValue().quantity());
        foodItemJson.put("unit", entry.getValue().unit().name());
      }

      return fridgeJson.toString();

    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Could not load fridge", e);
    }
  }


  /**
   * Updates the fridge belonging to the specified user.
   *
   * @param userName the username of the fridge owner
   * @param requestBody the JSON representation of the fridge
   * @return the result of the update request
   */



  @PutMapping("/fridges")
  public ResponseEntity<String> updateFridge(
      @RequestParam(name = "userName") String userName,
      @RequestBody String requestBody) {



    if (userName.isBlank()) {
      return ResponseEntity.badRequest().body("userName is required");
    }






    JsonNode requestJson;

    try {
      requestJson = objectMapper.readTree(requestBody);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      return ResponseEntity.badRequest().body("Invalid JSON");
    }



    if (requestJson == null || !requestJson.isObject()) {
      return ResponseEntity.badRequest().body("JSON object is required");
    }



    JsonNode foodItemsJson = requestJson.get("foodItems");

    if (foodItemsJson == null || !foodItemsJson.isArray()) {
      return ResponseEntity.badRequest()
          .body("foodItems must be an array");
    }




    Fridge fridge = new Fridge(new User(userName));

    for (JsonNode foodItemJson : foodItemsJson) {
      if (foodItemJson == null || !foodItemJson.isObject()) {
        return ResponseEntity.badRequest()
            .body("Each food item must be an object");
      }





      if (!foodItemJson.has("name")
          || !foodItemJson.has("quantity")
          || !foodItemJson.has("unit")) {
        return ResponseEntity.badRequest()
            .body("Food item name, quantity and unit are required");
      }





      if (!foodItemJson.path("name").isTextual()
          || !foodItemJson.path("quantity").isNumber()
          || !foodItemJson.path("unit").isTextual()) {
        return ResponseEntity.badRequest()
            .body("Invalid food item field types");
      }



      String foodItemName = foodItemJson.path("name").asText();

      if (foodItemName.isBlank()) {
        return ResponseEntity.badRequest()
            .body("Food item name must not be blank");
      }


      double quantity = foodItemJson.path("quantity").asDouble();

      if (!Double.isFinite(quantity) || quantity < 0) {
        return ResponseEntity.badRequest()
            .body("Food item quantity must be finite and non-negative");
      }


      String unitName = foodItemJson.path("unit").asText();

      if (unitName.isBlank()) {
        return ResponseEntity.badRequest()
            .body("Food item unit must not be blank");
      }



      Unit unit;

      try {
        unit = Unit.parseUnit(unitName);
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body("Unsupported food item unit: " + unitName);
      }



      try {
        FoodItem foodItem = new FoodItem(foodItemName);
        Amount amount = new Amount(quantity, unit);

        fridge.addFoodItem(foodItem, amount);
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body("Invalid food item: " + e.getMessage());
      }



    }



    try {
      fridgeFileHandler.save(Path.of(fridgesPath), fridge);

      return ResponseEntity.ok("Fridge updated successfully");
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Could not save fridge", e);
    }

  }

}
