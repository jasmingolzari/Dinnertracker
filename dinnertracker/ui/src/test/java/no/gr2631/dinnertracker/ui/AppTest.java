package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class AppTest {

  @Test
  void appCanBeCreated() {
    assertDoesNotThrow(App::new);
  }
}
