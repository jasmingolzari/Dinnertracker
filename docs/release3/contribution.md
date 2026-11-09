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

Throughout the project, I have contributed across all three modules—core, persistence, and UI—with a focus on code quality, maintainability, and teamwork.

- Release 1: I implemented Fridge.java and FoodItem.java with corresponding tests and introduced continuous integration (CI) to the project.
- Release 2: I focused on refactoring and reducing code duplication. I worked on module-info.java across all three modules, GenericRepository.java, FridgeFileHandler.java, FridgePageController.java and its FXML file, and ShowAlertMessages.java. I also integrated Ikonli icons, introduced Mockito for controller testing instead of TestFX, and proposed the Unit and Amount classes.
- Release 3: I implemented User.java, RecipeBook.java, UsersFileHandler.java, and UserLogInPageController.java with its corresponding FXML file.

Beyond implementation, I worked on improving code quality by applying the DRY principle, generalizing reusable functionality, and ensuring consistent UI design across pages. I also improved our workflow by introducing GitHub pull request and issue templates and establishing clearer development conventions. Regarding teamwork, I actively participated in planning, weekly meetings, and discussions, helping maintain clear expectations, effective communication, and collaboration throughout the project.