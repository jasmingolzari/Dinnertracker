package no.ntnu.it1901.gr2631.dinnertracker.persistence;

public class RecipesPersistenceTest {
    @TempDir 
    Path tempDir; 

    @Test
    void testSaveRecipeCreatesFile() throws IOException{
        RecipeFileHandler recipeFileHandler = new ReipeFileHandler(); 
        Recipe recipe = new Recipe(); 
        Path path = tempDir.resolve("recipe.json"); 
        
        recipeFileHandler.saveRecipe(recipe, path); 
        assertTrue(File.exists(path)); 
    }
}
