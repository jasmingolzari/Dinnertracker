
# Release 3

## Overview
Release 3 is the final release of Dinnertracker. It completes the application by adding a REST API, user login and a set of new features that make the app more useful in everyday cooking. It also finishes all the work that was listed as future work in Release 2.

The main idea behind this release is to help the user cook with what they already have. The recipe list is now sorted by what is in the user's fridge, each recipe can be opened in a detailed view, and using a recipe removes the ingredients from the fridge automatically.

The release also focuses on code quality, with all Checkstyle and SpotBugs checks passing, consistent code formatting with Spotless, and test coverage measured with JaCoCo.


## Main goals
- Complete all future work from Release 2
- Add a REST API using Spring Boot
- Add user login and connect recipes and the fridge to the user
- Sort recipes based on the food items in the user's fridge
- Add a detailed view for each recipe
- Let the user "use" a recipe and update the fridge automatically
- Build with no errors from Checkstyle and SpotBugs
- Reach a test coverage of at least 80 % with JaCoCo
- Use Spotless to keep the code formatting consistent
- Reflect on the sustainability aspects of the project


## New in Release 3

### REST API
The project has a new `rest` module built with Spring Boot. It exposes the core and persistence layers over HTTP, so the data in Dinnertracker can be read and changed by other clients than the JavaFX application.

The API has three controllers, one for each main part of the domain:

| Method | Endpoint                     | Description                              |
|--------|------------------------------|------------------------------------------|
| GET    | `/health`                    | Checks that the server is running        |
| GET    | `/users?userName=...`        | Gets a user                              |
| POST   | `/users`                     | Creates a new user                       |
| GET    | `/recipes?userName=...`      | Gets the recipes for a user              |
| POST   | `/recipes`                   | Adds a new recipe                        |
| GET    | `/fridges?userName=...`      | Gets the fridge for a user               |
| PUT    | `/fridges?userName=...`      | Updates the fridge for a user            |

Every request that reads or changes data is tied to a `userName`, so a user only gets access to their own recipes and fridge. The controllers reuse the file handlers from the `persistence` module, which means the API and the JavaFX application read and write the same JSON data.

A more detailed description of the API, including request and response formats, is found in [Rest-API](restApi.md).

### User login
When the application starts, the user is met by a login page where they enter a username. If the user does not exist, a new user is created. After login, the user's fridge and recipes are loaded, and everything the user adds or changes is saved under their username. This makes it possible for several people to use the same installation of Dinnertracker without sharing a fridge.

### Recipes sorted by the fridge
The recipe overview is now sorted based on the content of the user's fridge. For each recipe the application counts how many of the ingredients the user is missing, and the recipes with the fewest missing ingredients appear at the top. Recipes the user can make right away are therefore shown first.

The list is updated automatically when the fridge changes, so the order always reflects what the user has at the moment. The purpose is to make it easier to use the groceries that are already at home, instead of buying
new ones.

### Detailed recipe view
The user can click on a recipe in the overview to open a separate page with the full recipe. The page shows:

- the name of the recipe
- all ingredients with amount and unit
- the steps in order

Each ingredient is marked based on whether the user has enough of it in the fridge, so the user can quickly see what is missing before they start cooking.

### Use a recipe 
From the detailed view, the user can choose to use the recipe. If all ingredients are available in the fridge, the required amounts are removed from the fridge and the change is saved. If something is missing, the user gets a message explaining that the recipe cannot be used. This keeps the fridge up to date without the user having to remove each item by hand.

## Architecture
The project is split into four Maven modules:

- **core** – the domain classes (`User`, `Fridge`, `Recipe`, `RecipeBook`,
  `FoodItem`, `Amount`, `Unit`)
- **persistence** – saving and loading users, fridges and recipes as JSON
- **ui** – the JavaFX application
- **rest** – the Spring Boot REST API (new in Release 3)



Both `ui` and `rest` depend on `core` and `persistence`, but not on each other. 

### Diagrams
The architecture is described in more detail in the following diagrams:

- **Package diagram** – shows the packages in the solution and how the
  modules depend on each other.
  [Package diagram](diagrams/packageDiagram.png)
- **Class diagram** – shows the most important classes in the system and
  the relationships between them, with focus on the core domain
  (`User`, `Fridge`, `Recipe`, `RecipeBook`, `FoodItem`, `Amount`, `Unit`).
  [Class diagram](diagrams/classDiagram.png)
- **Sequence diagram** – shows an important use case from start to end,
  and how the user's interaction in the interface connects to what happens
  inside the system, including the REST calls.
  [Sequence diagram](diagrams/sequenceDiagram.png)

## Testing and code quality
Testing and code quality are important parts of Release 3. The goal was to build the project without any errors from the code quality tools, and to reach a test coverage of at least 80 %.

- **Checkstyle and SpotBugs** run as part of the build with no errors.
- **JaCoCo** measures test coverage. We have checked and confirmed that all modules pass the check with above 80%. 
- **Spotless** keeps the code formatting consistent across the project [adjust: and is checked as part of the Maven build / can be run with `mvn spotless:apply`].
- New tests are added for the new functionality:
  - `UserRestControllerTest`, `RecipeRestControllerTest` and
    `FridgeRestControllerTest` for the REST API
  - `UserLogInPageControllerTest` for login
  - `RecipeViewPageControllerTest` for the sorted recipe overview
  - `RecipeDetailsViewPageControllerTest` for the detailed recipe view

### Automatic checks in pull requests
The project has an automatic pipeline that runs every time a pull request is opened or updated. The pipeline builds the project with Maven and runs all tests together with Checkstyle, SpotBugs and JaCoCo. A pull request should not be merged before all checks pass. This makes sure that new code does not break existing functionality, and that the code quality stays the same across the whole team.

Some pull requests in the project history show that checks have failed. This happened because new functionality was added before the tests for it were written. The test coverage then dropped below the limit, and the JaCoCo check failed. The missing tests were added in later pull requests, and all checks now pass on the main branch.


The project uses Maven to build and test the application and to run the code quality checks.

## Sustainability
We reflect on the sustainability aspects of Dinnertracker using the Sustainability Awareness Framework (SusAF), including a sustainability awareness diagram (SusAD). The new features in this release are closely connected to this: sorting recipes by the fridge and removing used ingredients automatically both help the user reduce food waste. The full reflection is available in [Sustainability](sustainability.md). 

## Completed from Release 2
All future work listed in Release 2 is now completed:

- User login is integrated into the project.
- Recipes and the fridge are connected to the user.
- The existing pages are further developed with accessibility in mind.
- Added recipes can be clicked and shown on their own page.

## Documentation
The following documents provide more detailed information about Release 3:

- [Teamwork](teamswork.md)
- [Rest-API](restApi.md)
- [Contribution](contribution.md)
- [Sustainability](sustainability.md)
- [Challenges](challenges.md)
