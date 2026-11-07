# Architecture

Dinnertracker is a modular application with four layers. The domain
logic, data persistence, graphical user interface and REST API are
separated into four Maven modules:

- **UI** – the JavaFX desktop client, with FXML views, controllers and a
  REST client.
- **Core** – the domain model and application logic.
- **Persistence** – saves and loads application data as JSON files
  using Jackson.
- **REST** – a Spring Boot server that exposes users, recipes and
  fridges through a REST API.

Core, persistence and UI are Java modules with their own
`module-info.java` (`no.gr2631.dinnertracker.core`,
`no.gr2631.dinnertracker.persistence` and `no.gr2631.dinnertracker.ui`).
REST is a regular Maven module without a `module-info.java`, so it runs
on the classpath, as is usual for a Spring Boot application.

## Architecture diagram

The diagram is the package diagram from [uml.md](uml.md).

~~~mermaid
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
~~~

Solid arrows between modules are Maven dependencies. The dashed
`HTTP / JSON` arrow is not a Maven dependency. It shows that the UI
talks to the REST server over HTTP at runtime. The other dashed arrows
point to the external libraries each module uses.

The JavaFX application is a client of the REST API. The UI controllers
never read or write the JSON files. Instead they use
`DinnertrackerRestClient`, which sends HTTP requests to the REST server.
The REST server then uses the persistence module to read and write the
files in the `data/` directory:

~~~text
UI controller → DinnertrackerRestClient → (HTTP / JSON) → REST controller → file handler → JSON file
~~~

The UI depends on core because the client converts the JSON responses
into core objects such as `Recipe`, `RecipeBook` and `Fridge`, and the
controllers work on those objects. The UI does not depend on
persistence. The UI and REST modules both use Jackson, but only to
build and read JSON messages. The persistence module depends on core
because it stores and loads core domain objects. The core module does
not depend on any of the other modules, nor on JavaFX, Spring or
Jackson.

## UI layer

The UI is built with JavaFX 21 and FXML. `App` starts the application
and holds the current user, the fridge, the recipe book and a shared
`DinnertrackerRestClient`. The client is the only class that talks to
the REST API. It sends HTTP requests to `http://localhost:7070` and
converts between JSON and core objects.

| Controller                        | Responsibility |
|-----------------------------------|----------------|
| `UserLogInPageController`         | Logs in or registers a user, and loads the user's fridge and all recipes. |
| `HomePageController`              | Navigates to the recipe and fridge pages. |
| `RecipeViewPageController`        | Lists the recipes of all users, sorted by how well they match the logged-in user's fridge. |
| `RecipeDetailsViewPageController` | Shows a recipe and lets the user use it. |
| `RecipePageController`            | Creates a new recipe. |
| `FridgePageController`            | Shows the fridge contents and adds food items. |

## Core layer

The core layer contains the domain model and application logic. The
main classes are:

- `User` – a user identified by a username.
- `Recipe` – a recipe with a name, an owner (`User`), ingredients and
  an ordered list of steps.
- `RecipeBook` – a collection of recipes. It can *use* a recipe, which
  removes the recipe's ingredients from a `Fridge`.
- `Fridge` – the food items a user has. Adding an item that already
  exists adds the quantities together if the units match.
- `FoodItem` – a food item identified by its name. Two food items are
  equal if their names match, ignoring case and surrounding whitespace.
  This matters because `FoodItem` is used as a map key.
- `Amount` – a record of a quantity, which must be finite and greater
  than zero, and a `Unit`.
- `Unit` – an enum of supported units, such as `GRAM` and `PIECE`.

Recipes and fridges both store ingredients as `Map<FoodItem, Amount>`,
so they can be compared directly when a recipe is used.

## Persistence layer

The persistence layer stores data as JSON using Jackson. Only the REST
module uses it.

- `RecipeFileHandler` – stores all recipes in `recipes.json`.
- `FridgeFileHandler` – stores the fridges of all users in
  `fridges.json`.
- `UsersFileHandler` – stores registered users in `users.json`.

`RecipeFileHandler` and `FridgeFileHandler` implement the common
interface `GenericRepository<T>`, which has the methods
`load(Path, User)` and `save(Path, T)`.

## REST layer

The REST module is a Spring Boot application that runs on port 7070. It
is the only entry point to the JSON data, and the JavaFX application is
its client.

| Endpoint                  | Description | Status codes |
|---------------------------|-------------|--------------|
| `GET /health`             | Checks that the server is running. | 200 |
| `GET /recipes?userName=…` | Returns the user's recipes. | 200, 400, 500 |
| `GET /recipes/all`        | Returns all recipes from all users. | 200, 500 |
| `POST /recipes`           | Creates a new recipe. | 201, 400, 500 |
| `GET /fridges?userName=…` | Returns the user's fridge. | 200, 400, 500 |
| `PUT /fridges?userName=…` | Replaces the contents of the user's fridge. | 200, 400, 500 |
| `GET /users?userName=…`   | Checks whether a user exists. | 200, 400, 404, 500 |
| `POST /users`             | Registers a new user. | 201, 400, 409, 500 |

Invalid input returns `400`, and errors when reading or writing files
return `500`. `404` (unknown user) and `409` (username already taken)
are only used by the user endpoints. `GET /recipes` and `GET /fridges`
return empty content for unknown users.

## Module dependencies

~~~text
UI
└── Core
REST
├── Core
└── Persistence
    └── Core
~~~

Core does not depend on any other module. Of the project's modules, UI
depends only on core, and it talks to REST over HTTP. REST depends on
core and persistence.