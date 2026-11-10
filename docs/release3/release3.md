# Release 3

## Overview
Release 3 is the final release of Dinnertracker. It completes the
application by adding a REST API, user login and a set of new features that
make the app more useful in everyday cooking. The application can also be
packaged as a shippable product with jlink and jpackage. It also finishes the work that was listed as future work in Release 2.

The main idea behind this release is to help the user cook with what they
already have. The recipe list is now sorted by what is in the user's fridge,
each recipe can be opened in a detailed view, and using a recipe removes the
ingredients from the fridge automatically.

The release also focuses on code quality, with all Checkstyle and SpotBugs
checks passing, consistent code formatting with Spotless, and test coverage
measured with JaCoCo.

## Main goals
- Complete the future work from Release 2
- Add a REST API using Spring Boot, and let the JavaFX application communicate with it
- Package the application as a shippable product with jlink and jpackage
- Add user login and connect the fridge and recipes to the user
- Sort recipes based on the food items in the user's fridge
- Add a detailed view for each recipe
- Let the user "use" a recipe and update the fridge automatically
- Build with no errors from Checkstyle and SpotBugs
- Reach at least 80 % line coverage and 70 % branch coverage with JaCoCo
- Use Spotless to keep the code formatting consistent
- Reflect on the sustainability aspects of the project

## New in Release 3

### REST API
The project has a new `rest` module built with Spring Boot. The JavaFX
application communicates with the server through the REST API, using the
`DinnertrackerRestClient` class in the `ui` module. The server uses the
`core` and `persistence` modules to handle the domain logic and to read and
write the JSON data.

The API has three controllers, one for each main part of the domain:

| Method | Endpoint                | Description                       |
|--------|-------------------------|-----------------------------------|
| GET    | `/health`               | Checks that the server is running |
| GET    | `/users?userName=...`   | Gets a user                       |
| POST   | `/users`                | Creates a new user                |
| GET    | `/recipes?userName=...` | Gets the recipes for a user       |
| GET    | `/recipes/all`          | Gets all recipes from all users   |
| POST   | `/recipes`              | Adds a new recipe                 |
| GET    | `/fridges?userName=...` | Gets the fridge for a user        |
| PUT    | `/fridges?userName=...` | Updates the fridge for a user     |

Fridges are separated per username: `GET /fridges` only returns the fridge
that belongs to the given `userName`. Recipes are stored with the username of
the user who created them, but all recipes are shared between users, and the
JavaFX application uses `GET /recipes/all` to show them. There is no
authentication, so the username works as an identifier and not as a form of
access control.

The REST server must be running before the JavaFX application is started.
A more detailed description of the REST API, including request and response
formats, is found in [REST API](api.md).

### User login
When the application starts, the user is met by a login page where they
enter a username. If the user does not exist, a new user is created through
the REST API. After login, the user's fridge is fetched from the server, and
changes to the fridge are saved under their username. This means that
several people can use the same installation of Dinnertracker without
sharing a fridge.

Recipes are shared between all users. The recipe overview shows the recipes
of every user, sorted by how well they match the logged-in user's fridge.
This gives each user more recipes to choose from.

### Recipes sorted by the fridge
The recipe overview is now sorted based on the content of the user's
fridge. For each recipe, the application counts how many of the
ingredients are missing or not available in the required amount. Recipes
the user can make right away are shown first, followed by the recipes with
the fewest missing ingredients. Recipes with the same number of missing
ingredients are sorted by name.

The list is updated automatically when the fridge changes, so the order
always reflects what the user has at the moment. The purpose is to make it
easier to use the groceries that are already at home, instead of buying
new ones.

### Detailed recipe view
The user can click on a recipe in the overview to open a separate page with
the full recipe. The page shows:

- the name of the recipe
- all ingredients with amount and unit
- the steps in order

Each ingredient is marked based on whether the user has enough of it in the
fridge, so the user can quickly see what is missing before they start
cooking.

### Use a recipe
From the detailed view, the user can choose to use the recipe. If all
ingredients are available in the fridge, the required amounts are removed
from the fridge and the updated fridge is saved through the REST API. If
something is missing, the user gets a message explaining that the recipe
cannot be used. This keeps the fridge up to date without the user having to
remove each item by hand.

### Improvements to existing pages
The existing pages have received some smaller visual improvements:

- buttons and clickable list items show a hand cursor on hover
- the recipe names are coloured based on whether the recipe can
  be made with the current fridge contents
- the recipe overview is sorted and clickable, which makes the list easier
  to use than in Release 2

### Shippable product
The JavaFX client is packaged as a shippable product, so users can install
and run Dinnertracker without Maven, an IDE or a separate Java
installation. Only the client is packaged, since it is the part that is
distributed to users. The REST server is not distributed and is started
as described in the [README](../../README.md).

The client is packaged in two steps:

- **jlink** creates a small, custom Java runtime that only contains the
  modules the client needs (Java, JavaFX, Jackson, `core` and `ui`). This
  is possible because the modules have a `module-info.java`.
- **jpackage** wraps this runtime and the application into a native
  installer for the user's operating system:
  - **Windows:** an installer (`.exe`). The user runs the installer, and
    Dinnertracker is installed like any other program.
  - **macOS:** a disk image (`.dmg`). The user opens it and drags
    Dinnertracker to the Applications folder.

How to build, install and open the application is described in the
[README](../../README.md).

## Architecture
The project is split into four Maven modules:

- **core** – the domain classes (`User`, `Fridge`, `Recipe`, `RecipeBook`,
  `FoodItem`, `Amount`, `Unit`)
- **persistence** – saving and loading users, fridges and recipes as JSON
- **rest** – the Spring Boot REST server (new in Release 3)
- **ui** – the JavaFX client

Dinnertracker uses a client-server architecture. The JavaFX client sends
HTTP requests to the REST server through `DinnertrackerRestClient` and
receives JSON responses. The REST server uses `core` for the domain logic and
`persistence` to read and write the JSON files. The `ui` module therefore no
longer accesses the data files directly.

```text
User → JavaFX client (ui) → REST server (rest) → persistence → JSON files
```

### Diagrams
The architecture is described in more detail in the diagrams in
[UML diagrams](uml.md). The diagrams are written in Mermaid and
include:

- **Package diagram** – shows the modules and packages in the solution and how they depend on each other. The arrow from `ui` to `rest` is HTTP communication, not a Java dependency.
- **Class diagram** – shows the central domain model in the `core` module and the relationships between the classes.
- **Sequence diagram** – shows the use case "Create a recipe", from the user's input in the JavaFX client, through `DinnertrackerRestClient` and the REST server, to the recipe being saved in `recipes.json`.

## Testing and code quality
Testing and code quality are important parts of Release 3. The goal was to
build the project without any errors from the code quality tools, and to
reach the coverage limits set in JaCoCo.

- **Checkstyle and SpotBugs** run as part of the build with no errors.
- **JaCoCo** measures test coverage. The build requires at least 80 % line
  coverage and 70 % branch coverage, and all modules pass both limits.
- **Spotless** keeps the code formatting consistent across the project. It
  is checked as part of the Maven build, and the code can be formatted with
  `mvn spotless:apply`.
- New tests are added for the new functionality:
  - `UserRestControllerTest`, `RecipeRestControllerTest` and
    `FridgeRestControllerTest` for the REST API
  - tests for `DinnertrackerRestClient`, called `DinnertrackerRestClientTest`, which check the communication
    between the JavaFX client and the server
  - `UserLogInPageControllerTest` for login
  - `RecipeViewPageControllerTest` for the sorted recipe overview
  - `RecipeDetailsViewPageControllerTest` for the detailed recipe view

### Automatic checks in pull requests
The project has an automatic pipeline (GitHub Actions) that runs every time
a pull request is opened or updated. The pipeline builds the project with
Maven and runs all tests together with Checkstyle, SpotBugs, Spotless and
JaCoCo. The JavaFX tests run with a virtual display (Xvfb), since the build
server has no screen. A pull request should not be merged before all checks
pass. This makes sure that new code does not break existing functionality,
and that the code quality stays the same across the whole team.

Some pull requests in the project history show that checks have failed.
This happened because new functionality was added before the tests for it
were written. The test coverage then dropped below the limits, and the
JaCoCo check failed. The missing tests were added in later pull requests,
and all checks now pass on the main branch.

### Building and running
The project uses Maven to build and test the application and to run the
code quality checks. The REST server must be started before the JavaFX
client. A step-by-step guide for building the project, running the tests,
starting the REST server and starting the JavaFX client is found in the
[README](../../README.md).

## Sustainability
We reflect on the sustainability aspects of Dinnertracker using the
Sustainability Awareness Framework (SusAF), including a sustainability
awareness diagram (SusAD). The new features in this release are closely
connected to this: sorting recipes by the fridge and removing used
ingredients automatically both help the user reduce food waste. The full
reflection is available in [Sustainability](sustainability.md).

## Use of AI tools
The group has used AI tools as support during the project, mainly to
understand errors and tool output, get suggestions for tests, and improve
the language and structure of the documentation. All code and text
suggested by AI has been reviewed and adjusted by the group before it was
added to the project. A more detailed description is found in
[AI Tools](../ai-tools.md).

## Completed from Release 2
The future work listed in Release 2 is now completed:

- User login is integrated into the project.
- The fridge and recipes are connected to the user.
- The existing pages are further developed with small visual improvements.
- A hand cursor is shown when hovering over buttons and clickable items.
- The list view on the recipe page is improved: recipes are sorted by the
  fridge and coloured based on availability.
- Added recipes can be clicked and shown on their own page.

## Documentation
The following documents provide more detailed information about
Release 3:

- [AI Tools](../ai-tools.md)
- [Architecture](architecture.md)
- [Challenges](challenges.md)
- [Contribution](contribution.md)
- [Code quality](code-quality.md)
- [REST API](api.md)
- [Sustainability](sustainability.md)
- [Teamwork](teamwork.md)
- [UML diagrams](uml.md)
- [Work practices](work-practices.md)
- [Workflow](workflow.md)

