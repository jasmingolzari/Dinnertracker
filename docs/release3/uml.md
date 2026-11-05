# UML Diagrams

This document presents UML diagrams that describe the structure and behaviour of Dinnertracker. It includes a package diagram, a class diagram of the most important parts of the application, and a sequence diagram illustrating an important use case.

## Package Diagram

The package diagram illustrates the main modules and packages in Dinnertracker and how they relate to each other.

<!-- Camilla: Legg til pakkediagram her! -->

## Class Diagram

The class diagram presents the most important classes in Dinnertracker and their relationships.

<!-- Camilla: Legg til klassediagram her!! -->


# Sequence diagram

## Use case: Create a recipe

The sequence diagram shows what happens when a user creates a recipe in the JavaFX client. `RecipePageController` validates the input and sends the recipe to the REST server through `DinnertrackerRestClient`. The server validates the request, loads the existing recipe book, adds the new recipe and saves the book to `recipes.json`. The client only adds the recipe to its local recipe book after the server has confirmed with HTTP 201.

```mermaid
sequenceDiagram
    actor User
    participant UI as JavaFX UI (RecipePage.fxml)
    participant Controller as RecipePageController
    participant LocalBook as RecipeBook (client)
    participant Client as DinnertrackerRestClient
    participant API as RecipeRestController
    participant Recipe as Recipe
    participant Book as RecipeBook (server)
    participant Persistence as RecipeFileHandler
    participant JSON as recipes.json

    User->>UI: Enter recipe details
    User->>Controller: Click "Legg til oppskrift"
    Controller->>Controller: Validate name, at least one ingredient, at least one step

    alt Invalid input
        Controller-->>UI: showErrorAlert("Ugyldig input")
        UI-->>User: Show error message
    else Valid input
        Controller->>Recipe: Create Recipe (name, user, ingredients, steps)
        Controller->>Client: createRecipe(recipe)
        Client->>API: POST /recipes (JSON)
        Note over API,Recipe: The server validates the body and builds its own Recipe (addIngredient, addStep) at the same time
        API->>API: Validate request body

        alt Invalid request body
            API-->>Client: HTTP 400 Bad Request
            Client-->>Controller: IOException (status 400)
            Controller-->>UI: showErrorAlert("Klarte ikke å lagre oppskriften")
            UI-->>User: Show error message
        else Valid request body
            API->>Persistence: load(path, user)
            Persistence->>JSON: Read recipes

            alt Load fails
                Persistence-->>API: IOException
                API-->>Client: HTTP 500 "Could not load recipes"
                Client-->>Controller: IOException (status 500)
                Controller-->>UI: showErrorAlert("Klarte ikke å lagre oppskriften")
                UI-->>User: Show error message
            else Load succeeds
                JSON-->>Persistence: Existing recipe data
                Persistence-->>API: RecipeBook
                API->>Book: addRecipe(recipe)
                API->>Persistence: save(path, recipeBook)
                Persistence->>JSON: Write all recipes

                alt Save fails
                    Persistence-->>API: IOException
                    API-->>Client: HTTP 500 "Could not save recipe"
                    Client-->>Controller: IOException (status 500)
                    Controller-->>UI: showErrorAlert("Klarte ikke å lagre oppskriften")
                    UI-->>User: Show error message
                else Save succeeds
                    API-->>Client: HTTP 201 Created
                    Client-->>Controller: Returns normally
                    Controller->>LocalBook: addRecipe(recipe)
                    Controller-->>UI: showInfoAlert("Suksess") and clearForm()
                    UI-->>User: Display confirmation
                end
            end
        end
    end
```

If the server cannot be reached or the request times out, `DinnertrackerRestClient` throws an `IOException` before any response arrives. The controller then shows the same error alert as in the error cases above, and the recipe is not added to the local recipe book.