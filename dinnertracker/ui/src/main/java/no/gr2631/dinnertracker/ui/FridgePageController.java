package no.gr2631.dinnertracker.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Unit;
import no.gr2631.dinnertracker.persistence.FridgeFileHandler;

/**
 * Controller for the fridge page.
 *
 * <p>Handles initialization of the fridge form, loading the current fridge contents from the JSON
 * storage file, adding food items, and navigating back to the home page. The table view is backed
 * by a list of row objects generated from the persisted fridge data.
 */
public class FridgePageController {
  private final FridgeFileHandler fridgeFileHandler;

  @FXML private TextField foodItemField;

  @FXML private Spinner<Double> quantitySpinner;

  @FXML private ComboBox<Unit> unitComboBox;

  @FXML private TableView<FridgeRow> fridgeTable;

  @FXML private TableColumn<FridgeRow, String> foodNameColumn;

  @FXML private TableColumn<FridgeRow, String> amountColumn;

  /** Name of the row that should receive temporary visual feedback after an update. */
  private String highlightedFoodName;

  /** Creates the FXML controller using the application's shared persistence handler. */
  public FridgePageController() {
    this(App.fridgeFileHandler);
  }

  /**
   * Creates a controller using the supplied persistence handler.
   *
   * @param fridgeFileHandler the handler used to save fridge data
   */
  FridgePageController(FridgeFileHandler fridgeFileHandler) {
    this.fridgeFileHandler = fridgeFileHandler;
  }

  /**
   * Initializes the quantity spinner, unit selector, and table bindings when the fridge page is
   * loaded.
   *
   * <p>The table columns are bound to a lightweight row model and the current JSON-backed fridge
   * data is loaded into the view immediately.
   */
  @FXML
  public void initialize() {
    quantitySpinner.setValueFactory(
        new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 0, 0.1));

    unitComboBox.getItems().setAll(Unit.values());
    unitComboBox.setValue(Unit.GRAM);
    unitComboBox.setCellFactory(
        comboBox -> {
          return new ListCell<>() {
            @Override
            protected void updateItem(Unit unit, boolean empty) {
              super.updateItem(unit, empty);
              setText(empty || unit == null ? null : unit.getSymbol());
              setStyle(empty ? "" : "-fx-cursor: hand;");
            }
          };
        });
    unitComboBox.setConverter(
        new StringConverter<Unit>() {
          @Override
          public String toString(Unit unit) {
            return unit == null ? "" : unit.getSymbol();
          }

          @Override
          public Unit fromString(String symbol) {
            for (Unit unit : Unit.values()) {
              if (unit.getSymbol().equals(symbol)) {
                return unit;
              }
            }
            return null;
          }
        });

    foodNameColumn.setCellValueFactory(
        cell -> new SimpleStringProperty(cell.getValue().foodName()));
    amountColumn.setCellValueFactory(
        cell -> new SimpleStringProperty(cell.getValue().amountText()));
    amountColumn.setStyle("-fx-alignment: CENTER_RIGHT;");

    fridgeTable.setRowFactory(
        tableView -> {
          TableRow<FridgeRow> row = new TableRow<>();
          row.itemProperty()
              .addListener(
                  (observable, oldItem, newItem) -> {
                    if (newItem != null
                        && highlightedFoodName != null
                        && newItem.foodName().equalsIgnoreCase(highlightedFoodName)) {
                      row.setStyle("-fx-background-color: rgba(47, 107, 59, 0.18);");
                      PauseTransition highlightPause = new PauseTransition(Duration.millis(900));
                      highlightPause.setOnFinished(
                          event -> {
                            if (row.getItem() == newItem) {
                              row.setStyle("");
                            }
                          });
                      highlightPause.play();
                    } else {
                      row.setStyle("");
                    }
                  });
          return row;
        });

    highlightFridgeItem(null);
  }

  /**
   * Navigates from the fridge page back to the home page.
   *
   * @param event action event generated by the back button
   * @throws IOException if the home page cannot be loaded
   */
  @FXML
  public void backToHomePage(ActionEvent event) throws IOException {
    try {
      Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

      stage.setScene(new Scene(root));
      stage.show();
    } catch (IOException | RuntimeException exception) {
      ShowAlertMessage.showErrorAlert(
          "Kunne ikke åpne hjemmesiden", null, "Feilen er: " + exception.getMessage());
    }
  }

  /**
   * Adds the food item entered by the user to the fridge.
   *
   * <p>After a successful addition, the table is refreshed and the affected food row receives a
   * short visual highlight. The food name is cleared, and the quantity spinner is reset to zero.
   * Invalid input is shown in an error dialog.
   *
   * @param event action event generated by the add-food-item button.
   */
  @FXML
  public void addFoodItem(ActionEvent event) {
    try {
      String foodName = foodItemField.getText().trim();
      double quantity = quantitySpinner.getValue();
      Unit unit = unitComboBox.getValue();
      FoodItem foodItem = new FoodItem(foodName);

      App.getFridge().addFoodItem(foodItem, new Amount(quantity, unit));
      save();
      App.notifyFridgeChanged();

      foodItemField.clear();
      quantitySpinner.getValueFactory().setValue(0.0);
      unitComboBox.setValue(Unit.GRAM);
      highlightFridgeItem(foodName);

    } catch (IllegalArgumentException exception) {
      ShowAlertMessage.showErrorAlert("Ugyldig vare", null, exception.getMessage());
    } catch (IOException exception) {
      ShowAlertMessage.showErrorAlert(
          "En uforventet feil oppsto", null, "Feilen er: " + exception.getMessage());
    }
  }

  /**
   * Loads the persisted fridge contents from the JSON file and populates the table view.
   *
   * <p>Each food item is converted to a row with the item name and the formatted quantity/unit text
   * shown in the table. The table is refreshed after loading so the latest persisted contents are
   * displayed immediately. If a food name is supplied, its row receives a brief highlight to make
   * the update visible.
   *
   * @param foodNameToHighlight name of the food item that was added or updated, or {@code null}
   */
  public void highlightFridgeItem(String foodNameToHighlight) {
    if (App.getFridge() == null) {
      fridgeTable.setItems(FXCollections.emptyObservableList());
      return;
    }
    try {
      highlightedFoodName = foodNameToHighlight;

      ObservableList<FridgeRow> rows = FXCollections.observableArrayList();
      for (var entry : App.getFridge().getFoodItems().entrySet()) {
        FoodItem foodItem = entry.getKey();
        Amount amount = entry.getValue();
        String amountText =
            String.format(
                Locale.forLanguageTag("nb-NO"),
                "%.1f %s",
                amount.quantity(),
                amount.unit().getSymbol());
        rows.add(new FridgeRow(foodItem.getName(), amountText));
      }

      fridgeTable.setItems(rows);
      fridgeTable.refresh();
    } catch (Exception exception) {
      fridgeTable.setItems(FXCollections.emptyObservableList());
      ShowAlertMessage.showErrorAlert("Kunne ikke laste kjøleskapet", null, exception.getMessage());
    }
  }

  /**
   * Saves the current fridge contents to the JSON file. The file path is relative to the project
   * root.
   *
   * @throws IOException if an error occurs while writing to the file
   */
  public void save() throws IOException {
    fridgeFileHandler.save(Path.of("..", "data", "fridges.json"), App.getFridge());
  }

  /**
   * Represents a single row in the fridge table.
   *
   * @param foodName the name of the food item
   * @param amountText the formatted quantity and unit string shown in the table
   */
  private record FridgeRow(String foodName, String amountText) {}
}
