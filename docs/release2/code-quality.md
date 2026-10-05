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

Checkstyle is configured in the parent pom.xml file
using the Maven Checkstyle Plugin and the
google_checks.xml ruleset.

The Checkstyle check runs automatically during the
Maven verify phase. The build fails if a Checkstyle
violation is detected.



## SpotBugs

SpotBugs is a static analysis tool used to detect
potential bugs in Java code.

SpotBugs is configured in the parent pom.xml file
using the SpotBugs Maven Plugin.

The analysis uses maximum effort and a medium
reporting threshold. The SpotBugs check runs
automatically during the Maven verify phase and
fails the build if configured violations are found.

## Spotless

Spotless is used to enforce consistent formatting
of the Java source code.

Spotless is configured in the parent pom.xml file
using the Spotless Maven Plugin. Java source files
are formatted using Google Java Format. The
configuration also removes unused imports, trims
trailing whitespace, and ensures that files end
with a newline.

The Spotless check runs automatically during the
Maven verify phase. If source files do not match
the required formatting, the build fails and the
affected files are reported.


## Running code quality checks

The project uses Maven to build, test, and run the
configured code quality checks.

Run the following command from the repository root:

    mvn -f dinnertracker/pom.xml verify

The verify phase runs the automated tests and the
configured Checkstyle, SpotBugs, Spotless, and
JaCoCo checks.

JaCoCo generates coverage reports and checks the
configured coverage requirements.

If a configured quality check fails, Maven reports
the violations and the build fails.


## Current verification results

Running `mvn -f dinnertracker/pom.xml verify`
currently results in BUILD FAILURE.

The build currently stops in the core module because
Spotless detects formatting violations. As a result,
the persistence and UI modules are skipped during
the full verify run.

To obtain current coverage results independently,
the following command was run:

    mvn -f dinnertracker/pom.xml test jacoco:report

This command completes successfully for all modules.

The current JaCoCo results are:

- Core line coverage: 91.9%
- Core branch coverage: 82.3%
- Persistence line coverage: 100%
- Persistence branch coverage: 100%

These results are above the configured JaCoCo
requirements of 80% line coverage and 70% branch
coverage for the core and persistence modules.

The current full Maven verify build is therefore
blocked by Spotless formatting violations rather
than by these JaCoCo coverage results.