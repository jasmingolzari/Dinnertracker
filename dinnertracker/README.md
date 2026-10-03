# Dinnertracker

## About the application

Dinnertracker is a dinner planning application designed to help users
keep track of the ingredients they have available in their fridge and manage their own
dinner recipes. It is a dinner planning application developed as part of the IT1901 course.

The long-term goal is to make it
easier to plan dinners based on the ingredients available to the user.

Release 1 focused mainly on recipe functionality, while Release 2 extends the application with fridge functionality, persistence, testing and a clearer separation between the different parts of the application.


## Application screenshot

![Dinnertracker application homepage](images/dinnertrackerhome.png)

*Screenshot of the Dinnertracker application - homepage.*



![Dinnertracker recipes page](images/recipes.png)

*Screenshot of the Dinnertracker application - recipes page.*


![Dinnertracker fridge page](images/fridge.png)

*Screenshot of the Dinnertracker application - fridge page.*

## User stories

### User story 1 – Adding a recipe

> As a user, I want to add a recipe with a name and ingredients, so that
> I can save my own dinner recipes.

### User story 2 – Managing fridge ingredients

> As a user, I want to add ingredients to my fridge, so that I can keep
> an overview of the ingredients I have available.

## Application architecture

Dinnertracker is organized as a multi-module Maven project with three
main architectural layers:

- **Core** – contains the domain logic and central concepts of the
  application, such as recipes, food items, and the fridge.
- **Persistence** – handles saving and loading application data using
  JSON files.
- **UI** – contains the graphical user interface, implemented using
  JavaFX and FXML.

The modules are located in the following directories:

```text
dinnertracker/
├── core/
├── persistence/
├── ui/
├── data/
└── pom.xml
```
This separation makes it possible to keep the application's domain logic independent from the user interface and file handling. More detailed information about the architecture can be found in [architecture.md](docs/release2/architecture.md).

The overall `pom.xml` manages the three modules, while each module has
its own `pom.xml` containing its module-specific configuration and
dependencies.


## Coverage with JaCoCo
 
The project uses JaCoCo to measure test coverage and enforce a minimum threshold during the Maven verification phase.
 
### Commands
 
```bash
mvn verify
mvn jacoco:report
```
 
### What this does
 
- Runs the test suite and collects coverage data.
- Generates an HTML coverage report for local inspection.
- Generates an XML report for CI and tooling integration.
- Fails the build if the configured coverage threshold is not met.
 
### Report locations
 
After running `mvn verify`, the reports are available in:
 
- `dinnertracker/core/target/site/jacoco/index.html`
- `dinnertracker/persistence/target/site/jacoco/index.html`
- `dinnertracker/ui/target/site/jacoco/index.html`
- `dinnertracker/target/site/jacoco-aggregate/index.html`
 
The aggregated report in the root module gives the combined coverage across all modules.
 
### Coverage threshold
 
The current minimum enforced coverage is:
 
- 80% line coverage
- 70% branch coverage
 
If the project falls below these values, `mvn verify` will fail.


## Application data

Application data is stored as JSON files in the [`data/`](data/) directory.

The project currently contains a `recipes.json` file for recipe data. The persistence module is responsible for saving and loading this data. More information about persistence can be found in [persistence.md](docs/release2/persistence.md)


## Development environment

The project is configured to support development using Eclipse Che.

The project can be opened in Eclipse Che using the following link:

[Open Dinnertracker in Eclipse Che](https://che.iaas.ntnu.no/#https://git.ntnu.no/IT1901-2026-groups/gr2631)



## Project documentation

Documentation for the individual releases is located in the
[`docs/`](../docs/) directory.

The documentation for Release 1 can be found
[here](../docs/release1/release1.md).

The documentation for Release 2 can be found [here](../docs/release2/release2.md).
