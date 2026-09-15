# Dinnertracker

This repository contains the source code and documentation for the
Dinnertracker project, developed as part of IT1901 at NTNU.

## Repository structure

The actual Dinnertracker application is located in the
[`dinnertracker/`](dinnertracker/) directory. It is organized as a
multi-module Maven project consisting of three architectural layers:

- [`core/`](dinnertracker/core/) – contains the domain classes and their tests.
- [`persistence/`](dinnertracker/persistence/) – handles saving application data.
- [`ui/`](dinnertracker/ui/) – contains the JavaFX user interface.
- [`data/`](dinnertracker/data/) – contains application data files.

Project documentation is located in the [`docs/`](docs/) directory.

The repository is structured as follows:

```text
gr2631/
├── dinnertracker/
│   ├── core/
│   ├── persistence/
│   ├── ui/
│   ├── data/
|   ├── README.md
│   └── pom.xml
│
├── docs/
│   └── release1/
│
├── .gitignore
└── README.md
```

For more information about the application and its functionality, see the
[`Dinnertracker README`](dinnertracker/README.md).

## Building and testing

Dinnertracker is built using Maven and requires **Java 17**.

From the `dinnertracker/` directory, the project can be tested with:

```bash
mvn test
```

The JavaFX application can be started with:

```bash
mvn javafx:run
```

## Documentation

Documentation for the project releases can be found in the
[`docs/`](docs/) directory.

The documentation for Release 1 is available
[here](docs/release1/release1.md).

