package no.gr2631.dinnertracker.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.Unit;

/** Handles reading and writing fridges to a JSON file. */
public class FridgeFileHandler implements GenericRepository<Fridge> {
  private final ObjectMapper objectMapper;

  /** Creates a file handler with indented JSON output. */
  public FridgeFileHandler() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  /**
   * Loads all fridges from the given path. If the file does not exist or is empty, an empty list is
   * returned.
   *
   * @param path the path to read from
   * @return all fridges stored at the path, or an empty list if the file is missing or empty
   * @throws IOException if the file cannot be read or contains invalid JSON
   */
  @Override
  public List<Fridge> loadAll(Path path) throws IOException {
    List<Fridge> fridgesList = new ArrayList<>();

    if (!path.toFile().exists() || path.toFile().length() == 0) {
      return fridgesList;
    }

    JsonNode root = objectMapper.readTree(path.toFile());
    JsonNode fridgesNode = root.has("fridges") ? root.get("fridges") : root;

    if (fridgesNode == null || fridgesNode.isNull()) {
      return fridgesList;
    }

    for (JsonNode fridgeNode : fridgesNode) {
      fridgesList.add(fromJson(fridgeNode));
    }
    return fridgesList;
  }

  /**
   * Loads the fridge at the zero-based index represented by the given string. TODO: Will change
   * this to load by user ID instead of index, since the index is not a stable identifier. FOR
   * Release 3.
   *
   * @param path the path to read from
   * @param index the zero-based index of the fridge to load
   * @return the fridge at the given index
   * @throws IOException if the fridges cannot be read
   * @throws IllegalArgumentException if the index is not an integer or is out of bounds
   */
  @Override
  public Fridge load(Path path, String index) throws IOException {
    int fridgeIndex;
    try {
      fridgeIndex = Integer.parseInt(index);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Kjøleskapets indeks må være et heltall: " + index, e);
    }

    List<Fridge> fridges = loadAll(path);
    if (fridgeIndex < 0 || fridgeIndex >= fridges.size()) {
      throw new IllegalArgumentException("Indeks utenfor gyldig område: " + index);
    }
    return fridges.get(fridgeIndex);
  }

  /**
   * Writes all fridges to the given path, replacing any existing content.
   *
   * @param fridgesList the fridges to save
   * @param path the path to write to
   * @throws IOException if the fridges cannot be written
   */
  @Override
  public void saveAll(List<Fridge> fridgesList, Path path) throws IOException {
    ObjectNode root = objectMapper.createObjectNode();
    ArrayNode array = objectMapper.createArrayNode();
    for (Fridge fridge : fridgesList) {
      array.add(toJson(fridge));
    }
    root.set("fridges", array);
    objectMapper.writeValue(path.toFile(), root);
  }

  /**
   * Serializes a fridge into the JSON structure used by the persistence layer.
   *
   * @param fridge the fridge instance to convert
   * @return a JSON object representing the fridge and its food items
   */
  private ObjectNode toJson(Fridge fridge) {
    ObjectNode node = objectMapper.createObjectNode();
    // TODO add user ID to the JSON representation of the fridge
    // node.put("UserID", fridge.getName());

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
   * @return the reconstructed fridge
   */
  private Fridge fromJson(JsonNode fridgeNode) {
    Fridge fridge = new Fridge();
    JsonNode foodItemsNode = fridgeNode.get("foodItems");

    if (foodItemsNode == null || foodItemsNode.isNull()) {
      return fridge;
    }

    for (JsonNode foodItemNode : foodItemsNode) {
      FoodItem item = new FoodItem(foodItemNode.get("name").asText());
      Amount amount =
          new Amount(
              foodItemNode.get("quantity").asDouble(),
              parseUnit(foodItemNode.get("unit").asText()));
      fridge.addFoodItem(item, amount);
    }

    return fridge;
  }

  /**
   * Resolves a serialized unit value to the corresponding {@link Unit} enum constant.
   *
   * <p>Support is included for both the enum names (for example, "PIECE") and the user-facing
   * symbols (for example, "stk").
   *
   * @param value the stored unit value
   * @return the matching unit enum constant
   * @throws IllegalArgumentException if the value does not match any supported unit
   */
  private Unit parseUnit(String value) {
    for (Unit unit : Unit.values()) {
      if (unit.name().equalsIgnoreCase(value) || unit.getSymbol().equalsIgnoreCase(value)) {
        return unit;
      }
    }
    throw new IllegalArgumentException("Ugyldig enhet: " + value);
  }
}
