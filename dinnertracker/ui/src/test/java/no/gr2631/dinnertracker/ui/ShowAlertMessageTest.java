package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.stage.Window;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Tests for the JavaFX alert helper methods. */
public class ShowAlertMessageTest {

  private static boolean toolkitStarted;

  /** Starts the JavaFX toolkit before the alert tests run. */
  @BeforeAll
  public static void startToolkit() {
    try {
      Platform.startup(() -> {});
      toolkitStarted = true;
    } catch (IllegalStateException exception) {
      toolkitStarted = true;
    } catch (Throwable exception) {
      toolkitStarted = false;
    }
  }

  /** Runs an action on the JavaFX application thread. */
  private void runOnFxThread(ThrowingRunnable action) throws Exception {
    assumeTrue(toolkitStarted, "JavaFX toolkit is not available");
    CountDownLatch completed = new CountDownLatch(1);
    AtomicReference<Throwable> failure = new AtomicReference<>();
    Platform.runLater(
        () -> {
          try {
            action.run();
          } catch (Throwable throwable) {
            failure.set(throwable);
          } finally {
            completed.countDown();
          }
        });

    assertTrue(completed.await(5, TimeUnit.SECONDS), "JavaFX action did not finish in time.");
    Platform.runLater(
        () -> {
          for (Window window : new ArrayList<>(Window.getWindows())) {
            window.hide();
          }
        });
    if (failure.get() != null) {
      throw new AssertionError("JavaFX action failed.", failure.get());
    }
  }

  /** Verifies that an error alert can be displayed without throwing. */
  @Test
  public void showErrorAlertShouldDisplay() throws Exception {
    runOnFxThread(
        () ->
            assertDoesNotThrow(
                () -> ShowAlertMessage.showErrorAlert("Feil", "Feilhode", "Dette er en feil.")));
  }

  /** Verifies that an information alert can be displayed without throwing. */
  @Test
  public void showInfoAlertShouldDisplay() throws Exception {
    runOnFxThread(
        () ->
            assertDoesNotThrow(
                () -> ShowAlertMessage.showInfoAlert("Info", null, "Dette er informasjon.")));
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
