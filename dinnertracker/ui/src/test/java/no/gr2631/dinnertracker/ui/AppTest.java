package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

/** Tests for {@link App}. */
class AppTest {

  /** Verifies that the JavaFX application class can be created without throwing an exception. */
  @Test
  void appCanBeCreated() {
    assertDoesNotThrow(App::new);
  }
}
