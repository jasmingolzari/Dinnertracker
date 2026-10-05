package no.gr2631.dinnertracker.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Utility class for displaying styled error and information dialogs in the JavaFX UI.
 *
 * <p>The helper keeps alert creation consistent across the application and ensures that dialog
 * titles, headers, and content are shown with the expected JavaFX alert types.
 */
public class ShowAlertMessage {

  /**
   * Displays an error dialog with the given title, optional header, and message content.
   *
   * @param title the title shown in the alert window
   * @param header the optional header text; may be {@code null}
   * @param content the main error message shown inside the dialog
   */
  public static void showErrorAlert(String title, String header, String content) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }

  /**
   * Displays an information dialog with the given title, optional header, and message content.
   *
   * @param title the title shown in the alert window
   * @param header the optional header text; may be {@code null}
   * @param content the informational text shown inside the dialog
   */
  public static void showInfoAlert(String title, String header, String content) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
