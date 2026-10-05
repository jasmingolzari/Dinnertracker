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

Recipe and fridge data is stored in JSON format. We chose this format because it makes the data clear and easy to store. Jackson is used by the persistence module to serialize and deserialize recipe objects.

## File format

The recipe file contains a JSON array where each element represents one recipe.

Each recipe contains a name, a list of food item objects, and a list of steps.

Example:
```json
[ {
  "name" : "Pannekaker",
  "ingredients" : [ {
    "name" : "Egg",
    "quantity" : 1.0,
    "unit" : "PIECE"
  }, {
    "name" : "Melk",
    "quantity" : 1.0,
    "unit" : "KILOGRAM"
  }, {
    "name" : "Mel",
    "quantity" : 1.0,
    "unit" : "KILOGRAM"
  } ],
  "steps" : [ "Bland sammen" ]
} ]
```

The fridge file contains all fridges which we will later use when we implement user support. It stores an array with fridges, fridges is a json object that stores name and amount.
```
[ {
  "foodItems" : [ {
    "name" : "Jordbær",
    "quantity" : 3.0000000000000004,
    "unit" : "KILOGRAM"
  }, {
    "name" : "Eple",
    "quantity" : 5.0,
    "unit" : "PIECE"
  } ]
} ]
```

## Storage model

Dinnertracker uses implicit storage, where recipe data is saved and loaded automatically by the application rather than through explicit open and save actions by the user.
This approach was chosen because Dinnertracker behaves as an application that manages its own data, rather than as a document editor where the user manually opens and saves files.
