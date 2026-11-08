# Group Contributions

This document provides an overview of each group member's contributions to the development of Dinnertracker. The contributions include for example implementation, testing, documentation, project planning, and other important work carried out throughout the project.

## Camilla My Hanh Do

Throughout the project, I have taken part in the group’s overall decisions and helped other group members where needed.

- Release 1: I built the first version of the recipe page in JavaFX. This included the RecipePage GUI, RecipeController, and the buttons for adding recipes and ingredients. I also wrote unit tests for RecipeFileHandler in persistence and helped implement the basic functionality.

- Release 2: I implemented RecipeViewPage and its controller, including loading recipes into the GUI. I also added a dropdown bar with controller logic to RecipePage. In the core module, I implemented the Amount record and the Unit enum. I then changed RecipeFileHandler to support the new structure, where ingredients are stored as Map<FoodItem, Amount> . I wrote tests for RecipeFileHandler, Amount and Unit, and fixed a naming bug in RecipeFileHandlerTest.
 
- Release 3: My focus in this release is documentation. I am updating architecture.md and writing uml.md, which contains a class diagram and a package diagram of the application. I am also writing challenges.md, which describes the main challenges the group faced. In addition, I am adding Javadoc to the REST controllers, the application class and their tests. 

## Jasmin Golzari

[Main contributions to the project.]

## Faustin Tamba Kebbe

[Main contributions to the project.]

## Mia Ngo Nguyen

[Main contributions to the project.]

## Tharanya Paramsothy 

In this project, I have tried to contribute to all modules.
 
- Release 1: I created the Recipe domain class in core, together with its tests. I also started on the first version of RecipePage and its controller with Camilla.
 
- Release 2: I refactored the Recipe class to store ingredients in a HashMap, and pair-programmed changes to RecipeFileHandler in persistence. In the UI, I continued working on the recipe pages. Together with Camilla, I implemented RecipeViewPage and developed RecipePage further, including the unit dropdown and the recipe list. I also wrote controller tests, improved the design of the recipe pages and pair-programmed ShowAlertMessage with Rumyana for error and information messages.
 
- Release 3: I added the /recipes/all endpoint to the REST module, so recipes are shared between all users. I was also responsible for creating DinnertrackerRestClient and changing all UI controllers to use the REST client instead of the file handlers. In addition, I wrote sustainability.md and release3.md.



## Rumyana Zhizhovska

Throughout the project, my primary responsibilities have included developing the fridge and login pages, implementing backend functionality, and writing corresponding tests. My contributions across the three releases include:

Release 1:
- Fridge.java
- FoodItem.java
- Add continuous integration
Release 2:
- Refactoring and generalizing existing code to reduce duplication and improve maintainability.
module-info.java in all 3 modules
- GenericRepository.java
- FridgeFileHandler.java
- FridgePageController.java and its corresponding FXML file.
- ShowAlertMessages.java
- Implemented the Ikons using the Ikonli dependency
- Added tests using Mockito instead of TestFX
- Proposed implementing: Unit.java and Amount.java
Release 3:
- User.java
- RecipeBook.java
- UsersFileHandler.java
- UserLogInPageController.java and its corresponding FXML file.

My main objective was to contribute across all three modules—core, persistence, and UI—while focusing on three key areas:
Code quality: I prioritized maintainability by refactoring redundant code, applying the DRY principle, and improving reusability. I also helped standardize the user interface to ensure consistency across pages developed by different team members.
Workflow: I improved our development process by introducing pull request and issue templates, helping establish clearer and more structured GitHub workflows.
Teamwork: I actively contributed to planning, communication, and coordination within the team. We had weekly meetings, shared Git conventions, and clear expectations, we maintained an organized development process. I also encouraged open discussions about challenges and opportunities for improvement.
Overall, I aimed to contribute not only through implementation but also by improving the project's overall quality and team collaboration.
 
 


