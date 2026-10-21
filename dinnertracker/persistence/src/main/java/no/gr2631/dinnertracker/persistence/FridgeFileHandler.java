package no.gr2631.dinnertracker.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.User;

/** Handles reading and writing fridges to a JSON file. */
public class FridgeFileHandler implements GenericRepository<Fridge> {
  private final ObjectMapper objectMapper;

  /** Creates a file handler with indented JSON output. */
  public FridgeFileHandler() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  /**
   * Load Fridge from the given path. If the file does not exist or is empty, an empty list is
   * returned.
   *
   * @param path the path to read from
   * @return The fridge stored at the path, or an empty list if the file is missing or empty
   * @throws IOException if the file cannot be read or contains invalid JSON
   */
  @Override
  public Fridge load(Path path, User userName) throws IOException {
    Fridge fridge = new Fridge(userName);

    if (!path.toFile().exists() || path.toFile().length() == 0) {
      return fridge;
    }

    JsonNode root = objectMapper.readTree(path.toFile());
    if (root == null || root.isNull()) {
      return fridge;
    }

    JsonNode fridgesNode = root.has("fridges") ? root.get("fridges") : root;

    if (fridgesNode == null || !fridgesNode.isArray()) {
      return fridge;
    }

    for (JsonNode fridgeNode : fridgesNode) {
      if (fridgeNode == null || fridgeNode.isNull()) {
        continue;
      }

      String storedUsername = fridgeNode.path("userName").asText();

      if (storedUsername.equals(userName.getUserName())) {
        return fromJson(fridgeNode, userName);
      }
    }

    return fridge;
  }

  /**
   * Saves a fridge while preserving the other users' fridges in the file.
   *
   * <p>Existing entries for the same username are replaced. Both a JSON array and an object
   * containing a {@code fridges} array are supported, matching {@link #load(Path, User)}.
   *
   * @param path the path to write to
   * @param fridge the fridge to save
   * @throws IOException if existing data cannot be read, has an unsupported structure, or the
   *     fridge cannot be written
   */
  @Override
  public void save(Path path, Fridge fridge) throws IOException {
    JsonNode root = objectMapper.createArrayNode();
    if (Files.exists(path) && Files.size(path) > 0) {
      root = objectMapper.readTree(path.toFile());
      if (root == null || root.isNull()) {
        root = objectMapper.createArrayNode();
      }
    }

    JsonNode storedFridges = root.has("fridges") ? root.get("fridges") : root;
    if (!storedFridges.isArray()) {
      throw new IOException("Expected a JSON array of fridges");
    }

    ArrayNode fridgesNode = (ArrayNode) storedFridges;
    String username = fridge.getUserName().getUserName();
    for (int i = fridgesNode.size() - 1; i >= 0; i--) {
      if (fridgesNode.get(i).path("userName").asText().equals(username)) {
        fridgesNode.remove(i);
      }
    }
    fridgesNode.add(toJson(fridge, fridge.getUserName()));
    objectMapper.writeValue(path.toFile(), root);
  }

  /**
   * Serializes a fridge into the JSON structure used by the persistence layer.
   *
   * @param fridge the fridge instance to convert
   * @return a JSON object representing the fridge and its food items
   */
  private ObjectNode toJson(Fridge fridge, User userName) {
    ObjectNode node = objectMapper.createObjectNode();
    node.put("userName", userName.getUserName());

    ArrayNode foodItemsNode = node.putArray("foodItems");
    for (Map.Entry<FoodItem, Amount> entry : fridge.getFoodItems().entrySet()) {
      ObjectNode ingredientNode = foodItemsNode.addObject();
      ingredientNode.put("name", entry.getKey().getName());
      ingredientNode.put("quantity", entry.getValue().quantity());
      ingredientNode.put("unit", entry.getValue().unit().name());
    }
    return node;
  }

  /**
   * Deserializes a fridge from its JSON representation.
   *
   * <p>The JSON node may contain food items with quantity values stored as numbers and unit values
   * as either enum names or unit symbols such as "stk" or "kg".
   *
   * @param fridgeNode the JSON object representing a single fridge
   * @param userName the user associated with the fridge
   * @return the reconstructed fridge
   */
  private Fridge fromJson(JsonNode fridgeNode, User userName) {
    Fridge fridge = new Fridge(userName);
    JsonNode foodItemsNode = fridgeNode.get("foodItems");

    if (foodItemsNode == null || foodItemsNode.isNull()) {
      return fridge;
    }

    for (JsonNode foodItemNode : foodItemsNode) {
      FoodItem item = new FoodItem(foodItemNode.get("name").asText());
      Amount amount =
          new Amount(
              foodItemNode.get("quantity").asDouble(),
              Unit.parseUnit(foodItemNode.get("unit").asText()));
      fridge.addFoodItem(item, amount);
    }
    return fridge;
  }
}
