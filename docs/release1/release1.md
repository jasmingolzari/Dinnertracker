# Release 1 – Dinnertracker

## 1. Project overview

Dinnertracker is a dinner planning application designed to help users keep track of the food they have available and find both inspiration and their own recipes. 

The application is developed as a modular Java project using JavaFX for
the graphical user interface. The project is divided into three main
architectural layers: domain logic, user interface, and persistence.

The goal of the first release is to create a small but functional
version of the application. The initial functionality focuses on adding
ingredients to a fridge, adding own recipes, and saving recipes using file storage.

The long-term goal of the application is to make it easier for users to
decide what to have for dinner by combining their available ingredients
with a collection of recipes.


## 2. Functionality

The planned functionality for Release 1 is:

- Add ingredients to the user's fridge.
- Add own personal recipes. 
- Save recipes using JSON file storage.
- Provide a simple JavaFX graphical user interface.

The application will later be extended with functionality such as, rating recipes, adding comments and links, handling
food expiration dates, and potentially retrieving recipes from online sources.

## 3. User stories

### User story 1 – Adding a recipe

As a user, I want to add a recipe with a name and ingredients, so that I
can save my own dinner recipes.

The functionality is divided into smaller development tasks, including:

- Recipe domain class (#3)
- Recipe persistence and saving recipes to file (#8)
- Tests for recipe file handling (#9)
- Recipe page GUI (#27)
- "Add recipe" button (#31)
- Tests for the Recipe class (#25)



### User story 2 – Managing fridge ingredients

As a user, I want to add ingredients to my fridge, so that I can keep
an overview of the ingredients I have available.

The functionality is divided into smaller development tasks, including:

- Food item domain class (#10)
- Tests for Food item (#15)
- Fridge domain class (#16)
- Fridge page GUI (#32)



### Perhaps future user story – Finding possible dinners

As a user, I want the application to show which recipes I can make based
on the ingredients in my fridge, so that I do not have to decide what
to make for dinner myself.

This functionality is planned for further development and possibilites, and is not the focus of Release 1.


## 4. Architecture

The application follows a three-layer architecture consisting of domain
logic, user interface, and persistence.

### Domain logic

The domain layer contains the core concepts and functionality of the
application, such as food items, recipes, and the fridge.

### User interface

The user interface is implemented using JavaFX. It will provide the user
with a graphical interface for interacting with the application.

### Persistence

The persistence layer is responsible for saving application
data using JSON files.

The separation between these layers makes it easier to develop and test
the different parts of the application independently.

## 5. Development and issues


Development tasks are managed using GitHub issues and project boards.
The issues consists of both functionality-related tasks and technical
or project-related tasks. Most of the issues consists of smaller tasks that can be implemented and tested independently. 

The issues corresponding to the initial user stories are used to
organize the implementation of user-facing functionality. These include
tasks related to recipes, fridge ingredients, the graphical user
interface, domain classes, persistence, and testing.

In addition to the user story-related issues, the project contains
technical and organizational issues. These include Maven and JavaFX
configuration, project structure, application startup, and
documentation.

The group uses branches for individual development tasks. Changes are
reviewed and merged into the main branch when they are considered ready.
The issues are linked to the Release 1 milestone and are used to keep
track of responsibility and progress within the group.


## 6. Testing

The project uses JUnit for automated testing.

Tests are located in the test source directories of the relevant Maven
modules. The tests cover functionality in the domain layer, including
food items, recipes, and the fridge.

The tests can be executed using Maven with:

    mvn test

The project is also configured with JaCoCo to report test coverage.

## 7. Maven and dependencies

The project uses Maven as its build system and is organized as a
multi-module Maven project.

The application can be tested with:

    mvn test

The JavaFX application can be started with:

    mvn javafx:run

The required Java version, Maven version, JavaFX dependencies, and other
relevant dependencies are specified in the project's Maven configuration.


## 8. Eclipse Che

The project is configured to support development using Eclipse Che.

The Eclipse Che workspace can be opened using the link provided in the
project's README.


## 9. AI Tools

AI tools have been used as a support during the development process,
primarily for troubleshooting, understanding unfamiliar concepts,
discussing possible solutions, and getting guidance when encountering
technical problems.

A more detailed description of the AI tools used and how they have
contributed to the project is available in
[`ai-tools.md`](ai-tools.md).


## 10. Future work

Several features are planned for later releases. These include:

- Loading recipes from JSON files. 
- Rating recipes using stars or dice rolls.
- Adding preparation time and difficulty.
- Adding comments and links to recipes.
- Retrieving existing recipes from online sources.
- Handling food expiration dates. 
- Allowing users to specify expiration times for certain food items.
- Improving the recipe matching/sorting functionality.
- Saving ingredients from fridge to file

