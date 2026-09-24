# Release 2

## Overview

Release 2 focuses on developing Dinnertracker into a complete
three-layer application with a clear separation between the UI, core
and persistence layers.

The release also focuses on improving persistence, testing, code quality,
documentation and the group's development practices.

## Main goals

- Complete and document the three-layer architecture
- Improve the modularization of the project
- Extend recipe and fridge functionality
- Improve persistence and file storage
- Add tests for the different modules and controllers
- Use JaCoCo to measure test coverage
- Introduce Checkstyle and SpotBugs for code quality
- Document the group's work practices and workflow
- Reflect on teamwork and collaboration

## Functionality

Release 2 extends the functionality developed in Release 1 and connects
the different parts of the application more closely.

The release includes further development of the recipe and fridge
functionality. This includes a recipe view with GUI controls, loading
recipes in the GUI, and a dedicated fridge page. Persistence is also
extended to support saving food items to file.

The project structure is developed into a modular three-layer
architecture consisting of the UI, core and persistence layers. The
different layers are connected so that the user interface can interact
with the core functionality and persistence layer.

## Testing and code quality

Testing and code quality are important parts of Release 2. Tests are
added across the different parts of the application, including tests
for the recipe, home page and fridge controllers, in addition to tests
for the core and persistence modules.

JaCoCo is used to measure test coverage. Checkstyle and SpotBugs are
also used as code quality tools.

The project uses Maven to build and test the application and to run the
code quality checks.

The setup and use of the testing and code quality tools are documented
in [Code Quality](code-quality.md).

## Documentation

The following documents provide more detailed information about
Release 2:

- [Architecture](architecture.md)
- [Persistence](persistence.md)
- [Work Practices](work-practices.md)
- [Workflow](workflow.md)
- [Code Quality](code-quality.md)
- [Teamwork](teamwork.md)
- [AI Tools](ai-tools.md)