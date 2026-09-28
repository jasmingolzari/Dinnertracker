# Architecture

Dinnertracker is structured as a modular three-layer application. The
application separates the graphical user interface, core domain logic,
and data persistence into separate Maven modules.

The three layers are:

- **UI** – handles the graphical user interface and user interaction.
- **Core** – contains the domain model and application logic.
- **Persistence** – handles saving and loading application data using JSON.

## Architecture diagram

~~~mermaid
flowchart TD
    UI[UI module<br/>JavaFX + FXML + Controllers]
    CORE[Core module<br/>Recipe + FoodItem + Fridge]
    PERSISTENCE[Persistence module]
    JSON[(JSON files)]

    UI --> CORE
    UI --> PERSISTENCE
    PERSISTENCE --> CORE
    PERSISTENCE <--> JSON
~~~

The UI module depends on both the core and persistence modules. The
persistence module depends on the core module because it stores and
loads core domain objects.

The core module does not depend on the UI or persistence modules.

## UI layer

The UI layer is implemented using JavaFX and FXML. It contains the
application entry point, views and controllers.

The controllers handle user interaction and coordinate calls to the
core and persistence layers. For example, `HomePageController` handles
navigation between the recipe and fridge pages, while
`RecipePageController` handles recipe input and saving.

## Core layer

The core layer contains the domain model and application logic. It
does not depend on JavaFX or the persistence implementation.

The main domain classes are:

- `Recipe`
- `FoodItem`
- `Fridge`

These classes represent recipes, food items and the contents of the
user's fridge.

## Persistence layer

The persistence layer handles storing and loading application data.
Dinnertracker uses JSON files and the Jackson library for persistence.

`RecipeFileHandler` is responsible for reading and writing recipe data.
It uses classes from the core module when handling recipes and food
items.


## Module dependencies

The module dependencies are:

~~~text
UI
├── Core
└── Persistence
    └── Core
~~~

The `core` module is independent of the other application modules.
The `persistence` module depends on `core`, while `ui` depends on both
`core` and `persistence`.