# Group Contributions

This document provides an overview of each group member's contributions to the development of Dinnertracker. The contributions include for example implementation, testing, documentation, project planning, and other important work carried out throughout the project.

## Camilla My Hanh Do

Throughout the project, I have taken part in the group’s overall decisions and helped other group members where needed.

- **Release 1:** I built the first version of the recipe page in JavaFX. This included the RecipePage GUI, RecipeController, and the buttons for adding recipes and ingredients. I also wrote unit tests for RecipeFileHandler in persistence and helped implement the basic functionality.

- **Release 2:** I implemented RecipeViewPage and its controller, including loading recipes into the GUI. I also added a dropdown bar with controller logic to RecipePage. In the core module, I implemented the Amount record and the Unit enum. I then changed RecipeFileHandler to support the new structure, where ingredients are stored as Map<FoodItem, Amount> . I wrote tests for RecipeFileHandler, Amount and Unit, and fixed a naming bug in RecipeFileHandlerTest.
 
- **Release 3:** My focus in this release is documentation. I am updating architecture.md and writing uml.md, which contains a class diagram and a package diagram of the application. I am also writing challenges.md, which describes the main challenges the group faced. In addition, I am adding Javadoc to the REST controllers, the application class and their tests. 

## Jasmin Golzari

Throughout the project, I have contributed to implementation, testing, documentation, and coordination, with a focus on a structured workflow.

- **Release 1:** I implemented the FoodItem domain class and its tests. I also wrote the initial Release 1 documentation and README files, covering setup, project structure, and how to run the application.

- **Release 2:** I took responsibility for documentation, writing the README files, release2.md, ai-tools.md, and architecture.md. With Mia, I pair-programmed workflow.md, work-practices.md, and teamwork.md. I also implemented FridgePageControllerTest for the UI's FridgePageController.

- **Release 3:** I updated both READMEs for the final release, including repository structure, architecture, screenshots, and corrections from reviews. I added instructions for running the shippable product, merged the AI tools documentation into one ai-tools.md, and co-wrote uml.md with Camilla (I created the first draft and sequence diagram, she created the package and class diagrams). I also put together contribution.md.

Beyond these tasks, I actively coordinated planning and group meetings throughout all releases. I prepared documentation and plans ahead of release meetings, so that the meetings ran smoothly, took initiative when progress slowed, supported group members, and encouraged continuous pull request reviews to improve code quality and collaboration.

## Faustin Tamba Kebbe

- **Release 1:** I established the initial multi-module Maven project structure, configuring the core, persistence, and UI modules. I also implemented RecipeFileHandler and its unit test to support recipe persistence.

- **Release 2:** I documented the project’s three-layer architecture and persistence mechanisisms. I also created and improved the code quality documentation, describing the project’s quality assurance practices.

- **Release 3:** I extended the Maven architecture by creating the fourth module, rest, and implemented the initial REST API. I subsequently migrated the rest module from Javalin to Spring Boot. I implemented and tested the fridge and users REST APIs, expanded REST controller test coverage, and improved UI test coverage. I also enhanced CI verification and Jacoco reporting, aplied Spotless formating and the REST API documentation. 

Finally, i updated the code-quality documentation, verified the latest Maven build, and adressed code review feedback.

## Mia Ngo Nguyen

I have contributed group project of developing Dinnertracker throughout all three releases including implementation, testing, documentation and teamwork.
 
- **Release 1:** My main focus for release 1 was implementing the homepage. This included UI using JavaFX, implementing the controller for navigation to recipes or fridge and writing corresponding unit tests in Unit.
 
- **Release 2:** For release 2 I co-authored several Markdown files, including workflow.md, work-practices.md and teamwork.md. I also updated the homepage UI to match our chosen theme colors and overall appearance making the UI consistent.
 
- **Release 3:** I implemented the first version detailed view page for a chosen recipe. This includes implementing functionality, tests and UI. This made it possible for the user to use a recipe from the recipe book and also use the items from the fridge with a button. Tests that did not have sufficient test coverage got updated and other issues related to testing and code quality were fixed. I also updated several Markdown files from release 2 to release 3.
 
For each release I have tried my best to contribute to ideas for functionality, and participated in discussions about design and implementation. I have followed the groups agreed work practices for GitHub including issues, branches, pull requests and conventional commits.

## Tharanya Paramsothy 

In this project, I have tried to contribute to all modules.
 
- **Release 1:** I created the Recipe domain class in core, together with its tests. I also started on the first version of RecipePage and its controller with Camilla.
 
- **Release 2:** I refactored the Recipe class to store ingredients in a HashMap, and pair-programmed changes to RecipeFileHandler in persistence. In the UI, I continued working on the recipe pages. Together with Camilla, I implemented RecipeViewPage and developed RecipePage further, including the unit dropdown and the recipe list. I also wrote controller tests, improved the design of the recipe pages and pair-programmed ShowAlertMessage with Rumyana for error and information messages.
 
- **Release 3:** I added the /recipes/all endpoint to the REST module, so recipes are shared between all users. I was also responsible for creating DinnertrackerRestClient and changing all UI controllers to use the REST client instead of the file handlers. In addition, I wrote sustainability.md and release3.md. 



## Rumyana Zhizhovska

Throughout the project, I have contributed across all three modules—core, persistence, and UI—with a focus on code quality, maintainability, and teamwork.

- **Release 1:** I implemented Fridge.java with corresponding tests and introduced continuous integration (CI) to the project.

- **Release 2:** I focused on refactoring and reducing code duplication. I worked on module-info.java across all three modules, GenericRepository.java, FridgeFileHandler.java, FridgePageController.java and its FXML file, and ShowAlertMessages.java. I also integrated Ikonli icons, introduced Mockito for controller testing instead of TestFX, and proposed the Unit and Amount classes.

- **Release 3:** I implemented User.java, RecipeBook.java, UsersFileHandler.java, and UserLogInPageController.java with its corresponding FXML file. Finally I packed the application to a shippable product in .exe and .app for both Windows and MacOS

Beyond implementation, I worked on improving code quality by applying the DRY principle, generalizing reusable functionality, and ensuring consistent UI design across pages. I also improved our workflow by introducing GitHub pull request and issue templates and establishing clearer development conventions. Regarding teamwork, I actively participated in planning, weekly meetings, and discussions, helping maintain clear expectations, effective communication, and collaboration throughout the project.