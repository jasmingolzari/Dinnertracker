
# Release 3

## Overview
Release 3 is the final release of Dinnertracker. It completes the
application by adding a REST API and user login, and by finishing all the
work that was listed as future work in Release 2.

The release also focuses on code quality, with all Checkstyle and SpotBugs
checks passing, consistent code formatting with Spotless, and a test
coverage of at least 80 % measured with JaCoCo.

## Main goals
- Complete all future work from Release 2
- Add a REST API using Spring
- Add user login and connect recipes and the fridge to the user
- Sort recipes based on the food items in the user's fridge
- Add a detailed view for each recipe
- Build with no errors from Checkstyle and SpotBugs
- Reach a test coverage of at least 80 % with JaCoCo
- Use Spotless to keep the code formatting consistent
- Reflect on the sustainability aspects of the project

## Functionality
- **REST API with Spring:** The application now has a REST API built with
  Spring, which lets the user interface communicate with the core and
  persistence layers [adjust: through endpoints for users, recipes and
  fridge items].
- **User login:** Users can log in, and the fridge and recipes are
  connected to the user's ID. Each user only has access to their own
  fridge.
- **Recipe sorting based on the fridge:** Recipes are sorted by how many of
  their ingredients the user already has in the fridge, so the recipe that
  best matches the user's groceries appears first. This helps the user use
  what they already have. 
- **Detailed recipe view:** The user can click on a recipe to open a page
  with the steps and the ingredients required.

The release also follows up the improvements planned in Release 2 for the
existing pages.

## Testing and code quality
Testing and code quality are important parts of Release 3. The goal was
to build the project without any errors from the code quality tools, and
to reach a test coverage of at least 80 %.

- **Checkstyle and SpotBugs** run with a successful build and no error
  messages.
- **JaCoCo** is used to measure test coverage, and the project reaches the
  goal of at least 80 % coverage.
- **Spotless** is added as an extra tool to keep the code formatting
  consistent across the project [adjust: and is checked as part of the
  Maven build / can be run with `mvn spotless:apply`].
- Tests are added for the new functionality, including the REST API, user
  login, recipe sorting and the recipe detail view.

The project uses Maven to build and test the application and to run the
code quality checks.

## Sustainability
We reflect on the sustainability aspects of Dinnertracker using the
Sustainability Awareness Framework (SusAF), including a sustainability
awareness diagram (SusAD). The reflection is available in
[Sustainability](sustainability.md).

## Completed from Release 2
All future work listed in Release 2 is now completed:

- User login is integrated into the project.
- Recipes and the fridge are connected to the user ID.
- The existing pages are further developed with accessibility in mind
  [adjust: cursor on button hover, improved table/list view].
- Added recipes can be clicked and shown on their own page.

## Documentation
The following documents provide more detailed information about
Release 3:

- [Teamwork](teamswork.md)
- [Rest-API](restApi.md)
- [Contribution](contribution.md)
- [Sustainability](sustainability.md)
- [Challenges](challenges.md)
