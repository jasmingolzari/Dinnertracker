package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class ControllerTest {

  @Test
  void controllerCanBeCreated() {
    assertDoesNotThrow(HomePageController::new);
    assertDoesNotThrow(RecipePageController::new);
  }
}
