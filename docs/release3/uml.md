# UML Diagrams

This document presents UML diagrams that describe the structure and behaviour of Dinnertracker. It includes a package diagram, a class diagram of the most important parts of the application, and a sequence diagram illustrating an important use case.

# Package diagram

The package diagram shows the main modules and packages in Dinnertracker and the dependencies between them. Each Maven module contains one Java package:

- **`core`** contains the domain model (`Recipe`, `RecipeBook`, `Fridge`, `FoodItem`, `Amount`, `Unit`, `User`) and the application's rules. It does not depend on any other module.
- **`persistence`** saves and loads data as JSON files in `data/` using Jackson. It depends on `core`, because it converts domain objects to and from JSON.
- **`rest`** is a Spring Boot server that exposes recipes, fridges and users through HTTP endpoints. It uses `core` for the domain logic and `persistence` for storage.
- **`ui`** contains the JavaFX application and its controllers. It uses `core` for the domain objects and `DinnertrackerRestClient` to send requests to the REST server.

The arrow from `ui` to `rest` is HTTP communication, not a Java dependency. Only the REST server reads and writes the JSON files. All Java dependencies point towards `core`, and there are no cycles between the modules.

```mermaid
flowchart TB
  subgraph UI["ui module"]
    U["no.gr2631.dinnertracker.ui<br/>App, DinnertrackerRestClient,<br/>UserLogInPageController, HomePageController,<br/>RecipePageController, RecipeViewPageController,<br/>RecipeDetailsViewPageController,<br/>FridgePageController, ShowAlertMessage"]
  end
  subgraph REST["rest module"]
    R["no.gr2631.dinnertracker.rest<br/>DinnertrackerApplication,<br/>RecipeRestController,<br/>FridgeRestController,<br/>UserRestController"]
  end
  subgraph PERSISTENCE["persistence module"]
    P["no.gr2631.dinnertracker.persistence<br/>GenericRepository, RecipeFileHandler,<br/>FridgeFileHandler, UsersFileHandler"]
  end
  subgraph CORE["core module"]
    C["no.gr2631.dinnertracker.core<br/>User, Recipe, RecipeBook, Fridge,<br/>FoodItem, Amount, Unit"]
  end
  JSON[("data/<br/>users.json, recipes.json, fridges.json")]
  FX[["JavaFX"]]
  JACKSON[["Jackson"]]
  SPRING[["Spring Boot"]]

  U -.->|"HTTP / JSON"| R
  U --> C
  R --> C
  R --> P
  P --> C
  P <-->|"read / write"| JSON
  U -.-> FX
  U -.-> JACKSON
  R -.-> SPRING
  R -.-> JACKSON
  P -.-> JACKSON
```

## Class Diagram
This diagram focuses on the central domain model in the `core` module. The domain model describes the concepts the application is built around: users, recipes, a recipe book, a fridge, and the food items and amounts they contain. The `core` module does not depend on JavaFX, Spring or the persistence module, so the domain logic can be used and tested independently of how data is shown or saved. The `ui`, `persistence` and `rest` modules all build on these classes.

The diagram shows the fields and public methods of each class and how the classes are related. UI controls, private helper methods (such as quantity validation) and `toString()` are left out to keep it readable.

```mermaid
classDiagram
  class User {
    -String userName
    +getUserName() String
  }
  class Recipe {
    -String name
    -Map~FoodItem, Amount~ ingredients
    -List~String~ steps
    -User userName
    +getName() String
    +getUserName() User
    +getIngredients() Map~FoodItem, Amount~
    +getSteps() List~String~
    +addIngredient(FoodItem item, Amount amount)
    +removeIngredient(FoodItem item)
    +addStep(String step)
    +removeStep(int index)
  }
  class RecipeBook {
    -List~Recipe~ recipes
    -User userName
    +addRecipe(Recipe recipe)
    +useRecipe(Recipe recipe, Fridge fridge)
    +getRecipes() List~Recipe~
    +getRecipeByName(String name) Recipe
    +getUserName() User
  }
  class Fridge {
    -Map~FoodItem, Amount~ foodItems
    -User userName
    +addFoodItem(FoodItem foodItem, Amount amount)
    +useFoodItem(FoodItem foodItem, Amount amount)
    +getQuantity(FoodItem foodItem) double
    +getUnit(FoodItem foodItem) Unit
    +getFoodItems() Map~FoodItem, Amount~
    +getUserName() User
  }
  class FoodItem {
    -String name
    +getName() String
    +equals(Object obj) boolean
    +hashCode() int
  }
  class Amount {
    <<record>>
    -double quantity
    -Unit unit
    +quantity() double
    +unit() Unit
  }
  class Unit {
    <<enumeration>>
    LITRE
    GRAM
    KILOGRAM
    PIECE
    TABLESPOON
    TEASPOON
    -String symbol
    +getSymbol() String
    +parseUnit(String value)$ Unit
  }

  RecipeBook "1" o-- "0..*" Recipe : recipes
  RecipeBook --> "1" User : userName
  Recipe --> "1" User : userName
  Fridge --> "1" User : userName
  Recipe --> "0..*" FoodItem : ingredients key
  Recipe --> "0..*" Amount : ingredients value
  Fridge --> "0..*" FoodItem : foodItems key
  Fridge --> "0..*" Amount : foodItems value
  Amount --> "1" Unit : unit
  RecipeBook ..> Fridge : useRecipe()
```

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
