package no.gr2631.dinnertracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import no.gr2631.dinnertracker.core.Amount;
import no.gr2631.dinnertracker.core.FoodItem;
import no.gr2631.dinnertracker.core.Recipe;
import no.gr2631.dinnertracker.core.Unit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Tests for RecipeDetailsViewPageController. */
class RecipeDetailsViewPageControllerTest {
  private static void runOnFxThread(Runnable action) throws Exception {
    if (Platform.isFxApplicationThread()) {
      action.run();
      return;
    }

    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Throwable> error = new AtomicReference<>();

    Platform.runLater(
        () -> {
          try {
            action.run();
          } catch (Throwable t) {
            error.set(t);
          } finally {
            latch.countDown();
          }
        });

    if (!latch.await(5, TimeUnit.SECONDS)) {
      throw new AssertionError("JavaFX operation timed out");
    }

    if (error.get() != null) {
      throw new AssertionError("JavaFX operation failed", error.get());
    }
  }

  @BeforeAll
  static void initializeJavaFx() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException e) {
      // JavaFX toolkit is already initialized.
    }
  }

  @Test
  void controllerCanBeCreated() {
    RecipeDetailsViewPageController controller = new RecipeDetailsViewPageController();

    assertNotNull(controller);
  }

  @Test
  void setRecipeDisplaysRecipeName() throws Exception {
    runOnFxThread(
        () -> {
          try {
            RecipeDetailsViewPageController controller = new RecipeDetailsViewPageController();

            Recipe recipe = new Recipe("Pasta", null);

            Label nameLabel = new Label();
            VBox ingredientList = new VBox();
            VBox stepsList = new VBox();

            Field nameField =
                RecipeDetailsViewPageController.class.getDeclaredField("recipeNameLabel");
            nameField.setAccessible(true);
            nameField.set(controller, nameLabel);

            Field ingredientsField =
                RecipeDetailsViewPageController.class.getDeclaredField("ingredientListVBox");
            ingredientsField.setAccessible(true);
            ingredientsField.set(controller, ingredientList);

            Field stepsField =
                RecipeDetailsViewPageController.class.getDeclaredField("stepsListVBox");
            stepsField.setAccessible(true);
            stepsField.set(controller, stepsList);

            controller.setRecipe(recipe);

            assertEquals("Pasta", nameLabel.getText());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Test
  void setRecipeDisplaysIngredientsWithAlternatingColors() throws Exception {
    runOnFxThread(
        () -> {
          try {
            RecipeDetailsViewPageController controller = new RecipeDetailsViewPageController();

            Recipe recipe =
                new Recipe(
                    "Tomatsalat",
                    null,
                    Map.of(
                        new FoodItem("Tomat"),
                        new Amount(2, Unit.PIECE),
                        new FoodItem("Agurk"),
                        new Amount(100, Unit.GRAM)),
                    List.of("Vask grønnsakene", "Kutt og bland"));

            Label nameLabel = new Label();
            VBox ingredientList = new VBox();
            VBox stepsList = new VBox();

            Field nameField =
                RecipeDetailsViewPageController.class.getDeclaredField("recipeNameLabel");
            nameField.setAccessible(true);
            nameField.set(controller, nameLabel);

            Field ingredientsField =
                RecipeDetailsViewPageController.class.getDeclaredField("ingredientListVBox");
            ingredientsField.setAccessible(true);
            ingredientsField.set(controller, ingredientList);

            Field stepsField =
                RecipeDetailsViewPageController.class.getDeclaredField("stepsListVBox");
            stepsField.setAccessible(true);
            stepsField.set(controller, stepsList);

            controller.setRecipe(recipe);

            assertEquals("Tomatsalat", nameLabel.getText());
            assertEquals(2, ingredientList.getChildren().size());
            assertEquals(2, stepsList.getChildren().size());

            CheckBox first = (CheckBox) ingredientList.getChildren().get(0);
            CheckBox second = (CheckBox) ingredientList.getChildren().get(1);

            assertEquals("-fx-background-color: #FFFFFF", first.getStyle());
            assertEquals("-fx-background-color: #E8F5E9", second.getStyle());

            assertTrue(first.getText().contains("stk") || first.getText().contains("g"));
            assertTrue(second.getText().contains("stk") || second.getText().contains("g"));

            assertEquals(
                "- Vask grønnsakene",
                ((Label) stepsList.getChildren().get(0)).getText().stripLeading());
            assertEquals(
                "- Kutt og bland",
                ((Label) stepsList.getChildren().get(1)).getText().stripLeading());

          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }
}
