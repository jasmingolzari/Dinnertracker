# Dinnertracker

## About the application

Dinnertracker is a dinner planning application developed as part of the IT1901 course at NTNU. It helps users manage dinner recipes and keep track of ingredients available in their fridge. 

The application supports user accounts, login, recipe management, and personal fridge management. Each user can manage their own fridge and the recipes available through the application.

Dinnertracker is developed in Java and uses JavaFX for the graphical user interface, a REST server for communication between the client and server, and JSON-based persistence for application data.

Release 1 focused mainly on recipe functionality, while Release 2 extended the application with fridge functionality, persistence, testing and a clearer separation between the different parts of the application. The application has since then, been extended with a REST API and further functionality such as sorting recipes based on available ingredients in their own fridge, and visualizing this in addition to a more detailed view of the recipes. 


## Application screenshot

The screenshots below show the application's user interface.


![Dinnertracker application login page](images/login.png)

*Screenshot of the Dinnertracker application - login page.*


![Dinnertracker application homepage](images/dinnertrackerhome.png)

*Screenshot of the Dinnertracker application - homepage.*


![Dinnertracker recipes page](images/recipes.png)

*Screenshot of the Dinnertracker application - recipes page.*

![Dinnertracker recipes page](images/pancakes.png)

*Screenshot of the Dinnertracker application - pancake recipe page*

![Dinnertracker recipes page](images/horn.png)

*Screenshot of the Dinnertracker application - horn recipe page*


![Dinnertracker recipes page](images/addRecipe1.png)
![Dinnertracker recipes page](images/addRecipe2.png)

*Screenshot of the Dinnertracker application - add recipe page.*


![Dinnertracker fridge page](images/fridge.png)

*Screenshot of the Dinnertracker application - fridge page.*



## Functionality and user stories

### User story 1 – Adding a recipe

> As a user, I want to add a recipe with a name and ingredients, so that
> I can save my own dinner recipes.


### User story 2 – Managing fridge ingredients

> As a user, I want to add ingredients to my fridge, so that I can keep
> an overview of the ingredients I have available.


### User story 3 - Detailed view of recipes

> As a user, I want to view an overview of all available recipes, as well as click into a specific recipe, so that I can easily see which ingredients are needed and which steps to follow to prepare the meal.


### User story 4 - Sorting based on fridge ingredients

> As a user, I want to sort the recipes based on the food items I have in my refrigerator, so that I can use ingredients I already own, reduce food waste, and quickly figure out what to make for dinner.


### User accounts and login

Users can create an account and log in to the application. The application keeps track of the logged-in user so that user-specific functionality can be provided.

### Recipe management

Users can manage dinner recipes and their ingredients, making it easier to keep track of possible meals. Dinnertracker also sorts recipes by the proportion of ingredients available in the user's fridge, prioritizing recipes that can be prepared with the ingredients they already have. 

### Personal fridge

Each user has their own fridge, where they can manage the ingredients they have available. This provides an overview of available ingredients when planning meals.




## Application architecture

Dinnertracker is organized as a multi-module Maven project. The application separates the graphical user interface, REST API, core logic, and persistence.

The main modules have the following responsibilities:

- **`ui/`** – contains the graphical user interface implemented using JavaFX and FXML.
- **`rest/`** – provides the REST API used for communication between the client and server.
- **`core/`** – contains the application's domain model and core logic.
- **`persistence/`** – handles loading and saving application data.
- **`data/`** – contains application data files.

The intended client-server architecture is illustrated below:

```mermaid
flowchart TD
    USER[User]
    UI["JavaFX client<br/>UI module"]
    REST["REST server<br/>Spring Boot"]
    CORE["Core module<br/>Domain logic"]
    PERSISTENCE["Persistence module"]
    JSON[("JSON data files")]

    USER --> UI
    UI <-->|HTTP requests and JSON responses| REST
    REST --> CORE
    REST --> PERSISTENCE
    PERSISTENCE <--> JSON
    PERSISTENCE --> CORE
```

The application is structured so that the JavaFX user interface communicates with the REST API, while the REST module uses the core and persistence modules to handle application logic and data.

This separation makes the application's domain logic independent of the user interface and the way data is stored.

More detailed information about the architecture and design can be found in the [Release 3 documentation](../docs/release3/).


### Generate JaCoCo reports

```bash
mvn verify
mvn jacoco:report
```

JaCoCo is used to measure test coverage and enforce minimum coverage thresholds during the Maven verification phase. The project configuration sets minimum thresholds of:

- 80% line coverage
- 70% branch coverage

The generated reports are available in the relevant modules' `target/site/jacoco/` directories. An aggregated report may also be available under `target/site/jacoco-aggregate/`, depending on the Maven configuration. The aggregated report provides an overview of test coverage across the modules.


## Application data

Application data is stored as JSON files in the `data/` directory.

The project currently uses the following files:

- `recipes.json` – stores recipe data.

- `fridges.json` – stores fridge data.

- `users.json` – stores user data.



## Development environment

The project uses Maven and is configured for development with Eclipse Che.

For the repository's Eclipse Che setup, see the [root README](../README.md).

## Project documentation

Documentation for the individual releases is located in the [`docs/`](../docs/) directory.

- [Release 1 documentation](../docs/release1/release1.md)
- [Release 2 documentation](../docs/release2/release2.md)
- [Release 3 documentation](../docs/release3/release3.md)
