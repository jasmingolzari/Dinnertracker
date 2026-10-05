package no.gr2631.dinnertracker.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Fridge;
import no.gr2631.dinnertracker.core.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests reading and writing fridges with {@link FridgeFileHandler}. */
class FridgeFileHandlerTest {

  private FridgeFileHandler handler;

  @TempDir Path tempDir;

  /** Creates a fresh file handler before each test. */
  @BeforeEach
  void setUp() {
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
    Fridge fridge = new Fridge();
    fridge.addFoodItem(new FoodItem(foodName), new Amount(quantity, unit));
    return fridge;
  }

  /** Verifies that loading a missing file returns an empty list. */
  @Test
  void loadAllShouldReturnEmptyListWhenFileDoesNotExist() throws IOException {
    List<Fridge> fridges = handler.loadAll(tempDir.resolve("fridges.json"));

    assertTrue(fridges.isEmpty());
  }

  /** Verifies that loading an empty file returns an empty list. */
  @Test
  void loadAllShouldReturnEmptyListWhenFileIsEmpty() throws IOException {
    Path path = createFile("fridges.json", "");

    List<Fridge> fridges = handler.loadAll(path);

    assertTrue(fridges.isEmpty());
  }

  /** Verifies that saving and loading preserves food-item data. */
  @Test
  void saveAllAndLoadAllShouldPreserveFridge() throws IOException {
    Path path = tempDir.resolve("fridges.json");
    Fridge fridge = createFridge("Mel", 500, Unit.GRAM);

    handler.saveAll(List.of(fridge), path);

    List<Fridge> fridges = handler.loadAll(path);

    assertEquals(1, fridges.size());
    FoodItem flour = fridges.get(0).getFoodItems().keySet().iterator().next();
    assertEquals("Mel", flour.getName());
    assertEquals(500, fridges.get(0).getQuantity(flour));
    assertEquals(Unit.GRAM, fridges.get(0).getUnit(flour));
  }

  /** Verifies that loading by index returns the corresponding fridge. */
  @Test
  void loadShouldReturnFridgeAtIndex() throws IOException {
    Path path = tempDir.resolve("fridges.json");
    handler.saveAll(
        List.of(createFridge("Mel", 500, Unit.GRAM), createFridge("Sukker", 200, Unit.GRAM)), path);

    Fridge fridge = handler.load(path, "1");

    FoodItem sugar = fridge.getFoodItems().keySet().iterator().next();
    assertEquals("Sukker", sugar.getName());
    assertEquals(200, fridge.getQuantity(sugar));
  }

  /** Verifies that a non-numeric index is rejected. */
  @Test
  void loadShouldRejectNonNumericIndex() throws IOException {
    Path path = tempDir.resolve("fridges.json");
    handler.saveAll(List.of(createFridge("Mel", 500, Unit.GRAM)), path);

    assertThrows(IllegalArgumentException.class, () -> handler.load(path, "first"));
  }

  /** Verifies that a negative index is rejected. */
  @Test
  void loadShouldRejectNegativeIndex() throws IOException {
    Path path = tempDir.resolve("fridges.json");
    handler.saveAll(List.of(createFridge("Mel", 500, Unit.GRAM)), path);

    assertThrows(IllegalArgumentException.class, () -> handler.load(path, "-1"));
  }

  /** Verifies that an index beyond the stored fridges is rejected. */
  @Test
  void loadShouldRejectIndexBeyondList() throws IOException {
    Path path = tempDir.resolve("fridges.json");
    handler.saveAll(List.of(createFridge("Mel", 500, Unit.GRAM)), path);

    assertThrows(IllegalArgumentException.class, () -> handler.load(path, "1"));
  }

  /** Verifies that malformed JSON causes an I/O exception. */
  @Test
  void loadAllShouldRejectInvalidJson() throws IOException {
    Path path = createFile("fridges.json", "This is not valid JSON");

    assertThrows(IOException.class, () -> handler.loadAll(path));
  }
}
