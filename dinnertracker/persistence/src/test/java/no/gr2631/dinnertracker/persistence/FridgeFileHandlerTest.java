package no.gr2631.dinnertracker.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.core.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests reading and writing fridges with {@link FridgeFileHandler}. */
class FridgeFileHandlerTest {

  private FridgeFileHandler handler;
  private User userName = new User("testuser");

  @TempDir Path tempDir;

  /** Creates a fresh file handler before each test. */
  @BeforeEach
  void setUp() {
    userName = new User("testuser");
    handler = new FridgeFileHandler();
  }

  /** Creates a file in the temporary test directory with the given content. */
  private Path createFile(String fileName, String content) throws IOException {
    Path path = tempDir.resolve(fileName);
    Files.writeString(path, content);
    return path;
  }

  /** Creates a fridge containing one food item. */
  private Fridge createFridge(String foodName, double quantity, Unit unit) {
    Fridge fridge = new Fridge(userName);
    fridge.addFoodItem(new FoodItem(foodName), new Amount(quantity, unit));
    return fridge;
  }

  /** Verifies that loading a missing file returns an empty fridge. */
  @Test
  void loadShouldReturnEmptyFridgeWhenFileDoesNotExist() throws IOException {
    Fridge fridge = handler.load(tempDir.resolve("fridges.json"), userName);

    assertTrue(fridge.getFoodItems().isEmpty());
  }

  /** Verifies that loading an empty file returns an empty fridge. */
  @Test
  void loadShouldReturnEmptyFridgeWhenFileIsEmpty() throws IOException {
    Path path = createFile("fridges.json", "");

    Fridge fridge = handler.load(path, userName);

    assertTrue(fridge.getFoodItems().isEmpty());
  }

  /** Verifies that saving and loading preserves food-item data. */
  @Test
  void saveAndLoadShouldPreserveFridge() throws IOException {
    Path path = tempDir.resolve("fridges.json");
    Fridge fridge = createFridge("Mel", 500, Unit.GRAM);

    handler.save(path, fridge);

    fridge = handler.load(path, userName);

    assertEquals(1, fridge.getFoodItems().size());
    FoodItem flour = fridge.getFoodItems().keySet().iterator().next();
    assertEquals("Mel", flour.getName());
    assertEquals(500, fridge.getQuantity(flour));
    assertEquals(Unit.GRAM, fridge.getUnit(flour));
  }

  /** Updating or clearing one user's fridge preserves the other user's stored data. */
  @Test
  void saveShouldOverwriteExistingFridgeAndAllowClearing() throws IOException {
    Path path = tempDir.resolve("fridges.json");
    User otherUser = new User("other-user");
    Fridge otherFridge = new Fridge(otherUser);
    otherFridge.addFoodItem(new FoodItem("Egg"), new Amount(6, Unit.PIECE));
    handler.save(path, otherFridge);
    handler.save(path, createFridge("Mel", 500, Unit.GRAM));
    Fridge replacement = createFridge("Milk", 1.5, Unit.LITRE);

    handler.save(path, replacement);

    assertEquals(replacement.getFoodItems(), handler.load(path, userName).getFoodItems());
    assertEquals(otherFridge.getFoodItems(), handler.load(path, otherUser).getFoodItems());

    handler.save(path, new Fridge(userName));

    assertTrue(handler.load(path, userName).getFoodItems().isEmpty());
    assertEquals(otherFridge.getFoodItems(), handler.load(path, otherUser).getFoodItems());
    assertEquals(2, new ObjectMapper().readTree(path.toFile()).size());
  }

  /** Adding a new user preserves existing entries and the supported JSON container format. */
  @ParameterizedTest
  @ValueSource(
      strings = {
        "[{\"userName\":\"other-user\",\"foodItems\":[],\"note\":\"keep me\"}]",
        "{\"fridges\":[{\"userName\":\"other-user\",\"foodItems\":[],"
            + "\"note\":\"keep me\"}],\"version\":1}"
      })
  void saveShouldAddFridgeWithoutRemovingExistingData(String json) throws IOException {
    Path path = createFile("fridges.json", json);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode originalRoot = mapper.readTree(json);
    JsonNode originalFridges =
        originalRoot.has("fridges") ? originalRoot.get("fridges") : originalRoot;
    Fridge fridge = createFridge("Mel", 500, Unit.GRAM);

    handler.save(path, fridge);

    JsonNode savedRoot = mapper.readTree(path.toFile());
    JsonNode savedFridges = savedRoot.has("fridges") ? savedRoot.get("fridges") : savedRoot;
    assertEquals(2, savedFridges.size());
    assertEquals(originalFridges.get(0), savedFridges.get(0));
    assertEquals(originalRoot.get("version"), savedRoot.get("version"));
    assertEquals(fridge.getFoodItems(), handler.load(path, userName).getFoodItems());
  }

  /** Empty files and empty JSON containers can receive their first fridge. */
  @ParameterizedTest
  @ValueSource(strings = {"", "null", "[]", "{\"fridges\":[]}"})
  void saveShouldInitializeEmptyData(String json) throws IOException {
    Path path = createFile("fridges.json", json);
    Fridge fridge = createFridge("Mel", 500, Unit.GRAM);

    handler.save(path, fridge);

    assertEquals(fridge.getFoodItems(), handler.load(path, userName).getFoodItems());
  }

  /** Duplicate entries for the saved user are replaced by one current fridge. */
  @Test
  void saveShouldReplaceDuplicateUserEntries() throws IOException {
    Path path =
        createFile(
            "fridges.json",
            """
            [{"userName":"testuser","foodItems":[]},
             {"userName":"testuser","foodItems":[]}]
            """);
    Fridge fridge = createFridge("Mel", 500, Unit.GRAM);

    handler.save(path, fridge);

    assertEquals(1, new ObjectMapper().readTree(path.toFile()).size());
    assertEquals(fridge.getFoodItems(), handler.load(path, userName).getFoodItems());
  }

  /** Invalid existing data is rejected before the original file is overwritten. */
  @ParameterizedTest
  @ValueSource(strings = {"invalid JSON", "{}", "42", "{\"fridges\":null}", "{\"fridges\":{}}"})
  void saveShouldRejectInvalidDataWithoutOverwritingIt(String json) throws IOException {
    Path path = createFile("fridges.json", json);
    Fridge fridge = createFridge("Mel", 500, Unit.GRAM);

    assertThrows(IOException.class, () -> handler.save(path, fridge));

    assertEquals(json, Files.readString(path));
  }

  @ParameterizedTest
  @ValueSource(strings = {"null", "[]", "[null]", "[{}]", "{\"fridges\":null}"})
  void loadShouldHandleEmptyJsonData(String json) throws IOException {
    Path path = createFile("fridges.json", json);

    assertTrue(handler.load(path, userName).getFoodItems().isEmpty());
  }

  /** Duplicate ingredients in a wrapped fridge are combined for every accepted unit format. */
  @ParameterizedTest
  @ValueSource(strings = {"GRAM", "gram", "g"})
  void loadShouldMergeDuplicateFoodItemsAndAcceptUnitFormats(String unit) throws IOException {
    Path path =
        createFile(
            "fridges.json",
            """
        {"fridges":[null,{
          "userName":"%s",
          "foodItems":[
            {"name":"Mel","quantity":200,"unit":"%s"},
            {"name":"Mel","quantity":300,"unit":"%s"}
          ]
        }]}
        """
                .formatted(userName.getUserName(), unit, unit));

    Fridge fridge = handler.load(path, userName);

    assertEquals(1, fridge.getFoodItems().size());
    assertEquals(500, fridge.getQuantity(new FoodItem("Mel")));
    assertEquals(Unit.GRAM, fridge.getUnit(new FoodItem("Mel")));
  }

  /** Verifies that malformed JSON causes an I/O exception. */
  @Test
  void loadShouldRejectInvalidJson() throws IOException {
    Path path = createFile("fridges.json", "This is not valid JSON");

    assertThrows(IOException.class, () -> handler.load(path, userName));
  }
}
