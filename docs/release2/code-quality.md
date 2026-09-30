# Code Quality

This document describes the code quality tools and checks used in dinnertracker.

## JaCoCo

Dinnertracker uses JaCoCo version 0.8.13 to measure
Java code coverage.

JaCoCo is configured in the parent pom.xml file.

The project has the following coverage requirements:

- Minimum line coverage: 80%
- Minimum branch coverage: 70%

## Checkstyle

Checkstyle is a static analysis tool used to check
Java coding style and enforce coding conventions.

Checkstyle is not currently configured in the
Dinnertracker Maven project. Therefore, no
automated Checkstyle checks are performed.

## SpotBugs

SpotBugs is a static analysis tool used to detect
potential bugs in Java code.

SpotBugs is not currently configured in the
Dinnertracker Maven project. Therefore, no
automated SpotBugs checks are performed.

## Running code quality checks

The project uses Maven to build and test the
application.

Run the following command from the repository root:

    mvn -f dinnertracker/pom.xml verify

The verify phase runs the tests, generates JaCoCo
coverage reports, and checks the configured
coverage requirements.

If the coverage requirements are not met,
JaCoCo reports the violations and fails the build.

Checkstyle and SpotBugs are not currently
configured and are therefore not executed.

## Current verification results

Running `mvn -f dinnertracker/pom.xml verify`
currently results in BUILD FAILURE.

The JaCoCo coverage check fails in the
persistence module:

- Line coverage: 46% (required: 80%)
- Branch coverage: 28% (required: 70%)

The persistence tests pass, but the build
stops before the UI module is verified.

Additional tests are needed to meet the
configured coverage requirements.