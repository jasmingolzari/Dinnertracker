# Persistence

## Architecture

Dinnertracker uses a three-layer architecture consisting of the core, persistence, and user interface modules.

### Core

The core module contains the domain logic and domain classes, such as recipes, food items, and the fridge.

### Persistence

The persistence module is responsible for saving and loading domain data using JSON files.

### User interface

The user interface module contains the JavaFX application and controllers and uses the core and persistence modules.

## Module dependencies

The persistence module depends on the core module. The user interface module depends on both the core and persistence modules.

## JSON persistence

Recipe data is stored in JSON format. Jackson is used by the persistence module to serialize and deserialize recipe objects.

## File format

The recipe file contains a JSON array where each element represents one recipe.

Each recipe contains a name, a list of food item objects, and a list of steps.

Example:
```json
[
  {
    "name": "Pasta",
    "ingridients": [
  {
    "name": "Pasta"
  },
  {
    "name": "Tomato sauce"
  }
],
    "steps": [
      "Boil the pasta",
      "Add the tomato sauce"
    ]
  }
]
```

## Storage model

Dinnertracker uses implicit storage, where recipe data is saved and loaded automatically by the application rather than through explicit open and save actions by the user.
This approach was chosen because Dinnertracker behaves as an application that manages its own data, rather than as a document editor where the user manually opens and saves files.