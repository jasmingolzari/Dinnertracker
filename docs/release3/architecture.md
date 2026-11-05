# Architecture

Dinnertracker is a multi-module Maven project (Java 17). The application
separates the domain logic, data persistence, the graphical user
interface and the REST API into four Maven modules:

- **Core** – contains the domain model and application logic.
- **Persistence** – saves and loads application data as JSON files using
  Jackson.
- **UI** – the JavaFX desktop client, with FXML views and controllers.
- **REST** – a Spring Boot server that exposes users, recipes and
  fridges through a REST API.

Core, persistence and UI are Java modules with their own
`module-info.java` (`no.gr2631.dinnertracker.core`,
`no.gr2631.dinnertracker.persistence` and `no.gr2631.dinnertracker.ui`).

## Architecture diagram

~~~mermaid
flowchart TD
    UI[UI module<br/>JavaFX + FXML + Controllers]
    REST[REST module<br/>Spring Boot + RestControllers]
    PERSISTENCE[Persistence module<br/>FileHandlers + Jackson]
    CORE[Core module<br/>Recipe, RecipeBook, Fridge,<br/>FoodItem, Amount, Unit, User]
    JSON[(data/<br/>users.json<br/>recipes.json<br/>fridges.json)]

    UI --> CORE
    UI --> PERSISTENCE
    REST --> CORE
    REST --> PERSISTENCE
    PERSISTENCE --> CORE
    PERSISTENCE <--> JSON
~~~

Both the UI and the REST module depend on core and persistence. The
persistence module depends on core because it stores and loads core
domain objects. The core module does not depend on any of the other
modules, and it does not depend on JavaFX, Spring or Jackson.

In the current version the JavaFX client does not communicate with the
REST API. Both the UI and the REST server use the persistence module
directly, and they read and write the same JSON files in the `data/`
directory.

## Core layer

The core layer contains the domain model and application logic. The
main classes are:

- `User` – a user identified by a username.
- `Recipe` – a recipe with a name, an owner (`User`), ingredients stored
  as `Map<FoodItem, Amount>` and an ordered list of steps.
- `RecipeBook` – the collection of recipes belonging to a user. It can
  also *use* a recipe, which removes the recipe's ingredients from a
  `Fridge`.
- `Fridge` – the food items a user has, stored as
  `Map<FoodItem, Amount>`. Adding an item that already exists adds the
  quantities together, as long as the units match.
- `FoodItem` – a food item identified by its name.
- `Amount` – a record combining a quantity and a `Unit`. The quantity
  must be finite and greater than zero.
- `Unit` – an enum of supported units (`LITRE`, `GRAM`, `KILOGRAM`,
  `PIECE`, `TABLESPOON`, `TEASPOON`), each with a short symbol such as
  `g` or `stk`.

Ingredients in recipes and fridges both use `Map<FoodItem, Amount>`, so
recipes and fridge contents can be compared directly when a recipe is
used.

## Persistence layer

The persistence layer handles storing and loading application data as
JSON with the Jackson library. The conversion between domain objects
and JSON is done manually with Jackson's tree model, because
`Map<FoodItem, Amount>` cannot be mapped automatically in a useful way.

- `GenericRepository<T>` – a common interface with `load(Path, User)`
  and `save(Path, T)`.
- `RecipeFileHandler` – implements `GenericRepository<RecipeBook>` and
  stores all recipes in `recipes.json`. Each recipe stores its
  `userName`, `name`, `ingredients` and `steps`.
- `FridgeFileHandler` – implements `GenericRepository<Fridge>` and
  stores the fridges of all users in `fridges.json`. When a fridge is
  saved, only the entry for that user is replaced.
- `UsersFileHandler` – loads and saves registered users in
  `users.json`. Existing users are kept, and duplicate usernames are not
  added.

Units are written by their enum name (for example `GRAM`), and can be
read back from either the enum name or the symbol.

## UI layer

The UI layer is implemented with JavaFX 21 and FXML. `App` is the entry
point and opens the login page. It also holds the state shared by the
controllers: the current user, the user's fridge and recipe book, and
the persistence handlers.

The pages and their controllers are:

| View (FXML)                  | Controller                           | Responsibility |
|------------------------------|--------------------------------------|----------------|
| `UserLogInPage.fxml`         | `UserLogInPageController`            | Logs in an existing user or registers a new one, and loads the user's fridge and recipe book. |
| `HomePage.fxml`              | `HomePageController`                 | Navigation to the recipe and fridge pages. |
| `RecipeViewPage.fxml`        | `RecipeViewPageController`           | Lists the user's recipes and opens a selected recipe. |
| `RecipeDetailsViewPage.fxml` | `RecipeDetailsViewPageController`    | Shows ingredients and steps, and lets the user use the recipe, which removes the ingredients from the fridge. |
| `RecipePage.fxml`            | `RecipePageController`               | Creates a new recipe with ingredients, units and steps. |
| `FridgePage.fxml`            | `FridgePageController`               | Shows the fridge contents and adds food items. |

`ShowAlertMessage` is a helper class for showing error and information
dialogs. The controllers update the domain objects in core and save the
changes through the persistence handlers.

## REST layer

The REST module is a Spring Boot application started from
`DinnertrackerApplication`. It runs on port 7070, and the paths to the
data files are configured in `application.properties`.

| Controller             | Endpoint        | Description |
|------------------------|-----------------|-------------|
| `RecipeRestController` | `GET /health`   | Checks that the server is running. |
|                        | `GET /recipes?userName=…` | Returns the user's recipes. |
|                        | `POST /recipes` | Creates a new recipe. |
| `FridgeRestController` | `GET /fridges?userName=…` | Returns the user's fridge. |
|                        | `PUT /fridges?userName=…` | Replaces the user's fridge contents. |
| `UserRestController`   | `GET /users?userName=…` | Checks whether a user exists. |
|                        | `POST /users`   | Registers a new user. |

The controllers validate the JSON requests, build core objects and use
the file handlers from the persistence module to read and write data.
Invalid input returns `400 Bad Request`, an unknown user returns
`404 Not Found` and an existing user returns `409 Conflict`.

## Module dependencies

The module dependencies are:

~~~text
UI
├── Core
└── Persistence
    └── Core

REST
├── Core
└── Persistence
    └── Core
~~~

The `core` module is independent of the other application modules. The
`persistence` module depends on `core`, while both `ui` and `rest`
depend on `core` and `persistence`. The UI and REST modules do not
depend on each other.