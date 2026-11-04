# Dinnertracker REST API Documentation

## 1. Introduction

Dinnertracker provides a REST API implemented using Java and Spring Boot. The API enables communication between the application and its backend through HTTP requests.

The REST API supports three main resources:

- **Recipes:** Creating and retrieving recipes.
- **Fridges:** Retrieving and updating fridge contents.
- **Users:** Creating and retrieving users.

The API uses JSON for structured request and response data where applicable.

### Base URL

When running the application locally, the REST API is available at:

`http://localhost:7070`

### Technologies

- Java 17
- Spring Boot
- Spring Web
- Jackson for JSON processing
- Maven for building and testing
- JUnit and Spring Boot Test for automated testing

The API uses the existing persistence layer to read and write application data to JSON files.

## 2. Running the REST API Locally

### Prerequisites

Before starting the REST API, ensure that the following software is installed:

- Java Development Kit (JDK) 17
- Apache Maven
- Git (for cloning the repository)

### Starting the server

1. Open a terminal in the project repository.
2. Navigate to the `dinnertracker` directory, which contains the parent `pom.xml`.
3. Build the REST module and its dependencies:

   ```bash
   mvn -pl rest -am package -DskipTests
   ```

4. Navigate to the REST module:

   ```bash
   cd rest
   ```

5. Start the Spring Boot application:

   ```bash
   mvn spring-boot:run
   ```

The REST API is configured to run on port `7070`.

**Base URL:**

```text
http://localhost:7070
```

### Verifying that the server is running

Open another terminal and execute:

```bash
curl -i http://localhost:7070/health
```

A successful health check returns HTTP status `200 OK`.

### Stopping the server

To stop the Spring Boot application, return to the terminal where the server is running and press:

`Ctrl + C`

### Important notes

- The server must be running before sending HTTP requests.
- Port `7070` must be available.
- The application uses JSON files for persistence.
- When testing endpoints that modify data, use test data rather than important existing application data.

## 3. API Endpoints

### 3.1 Health Check

**Endpoint:** `GET /health`

**Description:**

The health check endpoint verifies that the Dinnertracker REST API is running and responding to HTTP requests.

**Request example:**

```bash
curl -i http://localhost:7070/health
```

**Expected HTTP status:**

`200 OK`
**Response body:**

```text
Dinnertracker REST API is running

**Notes:**

- No request body is required.
- No query parameters are required.
- This endpoint can be used to verify that the REST server is available before testing the other endpoints.

### 3.2 Retrieve Recipes

**Endpoint:** `GET /recipes`

**Description:**

Retrieves all recipes belonging to a specified user. Recipes are loaded from the JSON persistence file and returned as a JSON array.

**Required query parameter:**

| Parameter | Type | Description |
|---|---|---|
| `userName` | String | Username of the user whose recipes should be retrieved. |

**Request example:**

```bash
curl -i "http://localhost:7070/recipes?userName=TestUser"
```

**Example successful response (HTTP 200):**

```json
[
  {
    "userName": "TestUser",
    "name": "Pancakes",
    "ingredients": [
      {
        "name": "Flour",
        "quantity": 2.0,
        "unit": "KILOGRAM"
      }
    ],
    "steps": [
      "Mix the ingredients",
      "Cook the pancakes"
    ]
  }
]
```

This is an illustrative example. Actual results depend on the recipes stored for the specified user.

If the user has no recipes, the endpoint returns an empty JSON array:

```json
[]
```

**HTTP status codes:**

| Status | Description |
|---|---|
| `200 OK` | Recipes were retrieved successfully. |
| `400 Bad Request` | The `userName` parameter is missing or blank. |
| `500 Internal Server Error` | An error occurred while loading the recipes. |

**Notes:**

- The endpoint returns only recipes belonging to the requested user.
- Each recipe contains a username, recipe name, ingredients and preparation steps.
- Each ingredient includes a name, quantity and unit.
- The endpoint does not create or modify recipes.

### 3.3 Create Recipe

**Endpoint:** `POST /recipes`

**Description:**

Creates a new recipe for the specified user. The server validates the request body, adds the recipe to the user's recipe book and saves it through the persistence layer.

**Request body:**

The request must contain a JSON object with the following fields:

| Field | Type | Required | Description |
|---|---|---|---|
| `userName` | String | Yes | Username of the recipe owner. Must not be blank. |
| `name` | String | Yes | Name of the recipe. Must not be blank. |
| `ingredients` | Array | No | List of ingredients. |
| `steps` | Array | No | List of preparation steps. |

Each ingredient must contain a non-blank `name`, a numeric `quantity` greater than zero and a supported `unit`. Each preparation step must be a non-blank string.

**Example JSON request:**

```json
{
  "userName": "TestUser",
  "name": "Pancakes",
  "ingredients": [
    {
      "name": "Flour",
      "quantity": 2.0,
      "unit": "KILOGRAM"
    }
  ],
  "steps": [
    "Mix the ingredients",
    "Cook the pancakes"
  ]
}
```

**Example curl request (Git Bash):**

```bash
curl -i -X POST "http://localhost:7070/recipes" \
  -H "Content-Type: application/json" \
  -d '{"userName":"TestUser","name":"Pancakes","ingredients":[{"name":"Flour","quantity":2.0,"unit":"KILOGRAM"}],"steps":["Mix the ingredients","Cook the pancakes"]}'
```

**Successful response:**

HTTP status: `201 Created`

Response body:

```text
Recipe created successfully
```

**HTTP status codes:**

| Status | Description |
|---|---|
| `201 Created` | The recipe was successfully created and saved. |
| `400 Bad Request` | Invalid JSON, missing required fields, invalid ingredient data or invalid preparation steps. |
| `500 Internal Server Error` | An error occurred while loading or saving the recipe data. |

**Notes:**

- The recipe is associated with the username provided in the request body.
- Ingredient quantities must be greater than zero.
- The supported measurement units are defined in the `Unit` enum.
- The endpoint uses `RecipeFileHandler` to load and save recipes.
- The request modifies persistent data. Use a separate test data file when testing the endpoint manually.

### 3.4 Retrieve Fridge Contents

**Endpoint:** `GET /fridges`

**Description:**

Retrieves the fridge contents belonging to a specified user. The server loads the user's fridge through the persistence layer and returns its contents as a JSON object.

**Required query parameter:**

| Parameter | Type | Description |
|---|---|---|
| `userName` | String | Username of the fridge owner. Must not be blank. |

**Request example:**

```bash
curl -i "http://localhost:7070/fridges?userName=TestUser"
```

**Example successful response (HTTP 200):**

```json
{
  "userName": "TestUser",
  "foodItems": [
    {
      "name": "Milk",
      "quantity": 2.0,
      "unit": "LITER"
    },
    {
      "name": "Flour",
      "quantity": 1.0,
      "unit": "KILOGRAM"
    }
  ]
}
```

This is an illustrative example. Actual results depend on the user's stored fridge contents.

If the fridge contains no food items, the response has an empty `foodItems` array:

```json
{
  "userName": "TestUser",
  "foodItems": []
}
```

**HTTP status codes:**

| Status | Description |
|---|---|
| `200 OK` | Fridge contents were retrieved successfully. |
| `400 Bad Request` | The `userName` parameter is missing or blank. |
| `500 Internal Server Error` | An error occurred while loading the fridge data. |

**Notes:**

- The endpoint returns the fridge associated with the specified username.
- Each food item includes its name, quantity and measurement unit.
- The `foodItems` field is always represented as a JSON array in a successful response.
- The endpoint uses `FridgeFileHandler` to retrieve the fridge data.
- This endpoint only reads data and does not modify the fridge contents.

### 3.5 Update Fridge Contents

**Endpoint:** `PUT /fridges`

**Description:**

Updates the fridge contents belonging to a specified user. The server validates the submitted JSON data, creates a fridge with the provided food items and saves it through the persistence layer.

**Required query parameter:**

| Parameter | Type | Description |
|---|---|---|
| `userName` | String | Username of the fridge owner. Must not be blank. |

**Request body:**

The request must contain a JSON object with a `foodItems` array.

| Field | Type | Required | Description |
|---|---|---|---|
| `foodItems` | Array | Yes | List of food items to store in the fridge. |
| `name` | String | Yes, per item | Name of the food item. Must not be blank. |
| `quantity` | Number | Yes, per item | Finite, non-negative quantity. |
| `unit` | String | Yes, per item | Supported measurement unit. |

**Example JSON request:**

```json
{
  "foodItems": [
    {
      "name": "Milk",
      "quantity": 2.0,
      "unit": "LITER"
    },
    {
      "name": "Flour",
      "quantity": 1.0,
      "unit": "KILOGRAM"
    }
  ]
}
```

**Example curl request (Git Bash):**

```bash
curl -i -X PUT "http://localhost:7070/fridges?userName=TestUser" \
  -H "Content-Type: application/json" \
  -d '{"foodItems":[{"name":"Milk","quantity":2.0,"unit":"LITER"},{"name":"Flour","quantity":1.0,"unit":"KILOGRAM"}]}'
```

**Successful response:**

HTTP status: `200 OK`

Response body:

```text
Fridge updated successfully
```

**HTTP status codes:**

| Status | Description |
|---|---|
| `200 OK` | Fridge contents were successfully updated and saved. |
| `400 Bad Request` | Missing username, invalid JSON, missing `foodItems` array or invalid food item data. |
| `500 Internal Server Error` | An error occurred while saving the fridge data. |

**Notes:**

- The username is provided as a query parameter, not in the JSON request body.
- The `foodItems` array may be empty.
- Each food item must have a non-blank name, a finite non-negative quantity and a supported measurement unit.
- The submitted food items form the new fridge contents; this operation does not incrementally add items to the existing fridge.
- The endpoint uses `FridgeFileHandler` to save the updated fridge.
- This endpoint modifies persistent data. Use isolated test data when testing it manually.

### 3.6 Retrieve User

**Endpoint:** `GET /users`

**Description:**

Retrieves an existing user by username. The server searches the stored user data through the persistence layer and returns the username if the user exists.

**Required query parameter:**

| Parameter | Type | Description |
|---|---|---|
| `userName` | String | Username of the user to retrieve. Must not be blank. |

**Request example:**

```bash
curl -i "http://localhost:7070/users?userName=TestUser"
```

**Example successful response (HTTP 200):**

```text
TestUser
```

The response body contains the username as plain text rather than a JSON object.

**HTTP status codes:**

| Status | Description |
|---|---|
| `200 OK` | The requested user was found. |
| `400 Bad Request` | The `userName` parameter is missing or blank. |
| `404 Not Found` | No user exists with the specified username. |
| `500 Internal Server Error` | An error occurred while loading user data. |

**Notes:**

- The endpoint retrieves a single user by username.
- The username is provided as a query parameter.
- The response contains the username as plain text.
- The endpoint uses `UsersFileHandler` to access stored user data.
- This endpoint only reads data and does not modify existing users.

### 3.7 Create User

**Endpoint:** `POST /users`

**Description:**

Creates a new user in Dinnertracker. The server validates the submitted username, checks whether the user already exists and saves the new user through the persistence layer.

**Request body:**

The request must contain a JSON object with the following field:

| Field | Type | Required | Description |
|---|---|---|---|
| `userName` | String | Yes | Username of the new user. Must not be blank. |

**Example JSON request:**

```json
{
  "userName": "TestUser"
}
```

**Example curl request (Git Bash):**

```bash
curl -i -X POST "http://localhost:7070/users" \
  -H "Content-Type: application/json" \
  -d '{"userName":"TestUser"}'
```

**Successful response:**

HTTP status: `201 Created`

Response body:

```text
User created successfully
```

**HTTP status codes:**

| Status | Description |
|---|---|
| `201 Created` | The user was successfully created and saved. |
| `400 Bad Request` | Invalid JSON, missing username, invalid username type or blank username. |
| `409 Conflict` | A user with the specified username already exists. |
| `500 Internal Server Error` | An error occurred while saving the user data. |

**Notes:**

- The request body must be a valid JSON object.
- The `userName` field must contain a non-blank string.
- Usernames must be unique in the stored user data.
- The endpoint uses `UsersFileHandler` for persistence.
- The successful response is plain text rather than JSON.
- This endpoint modifies persistent data. Use isolated test data when testing it manually.


## 4. API Error Handling

The Dinnertracker REST API uses standard HTTP status codes to indicate whether a request was successful or an error occurred.

### 4.1 HTTP Status Codes

| Status code | Meaning | Usage |
|---|---|---|
| `200 OK` | Successful request | Retrieving recipes, fridge contents or users, and updating fridge contents. |
| `201 Created` | Resource created | Successfully creating a recipe or user. |
| `400 Bad Request` | Invalid request | Missing required parameters, malformed JSON or invalid field values. |
| `404 Not Found` | Resource not found | Retrieving a user that does not exist. |
| `409 Conflict` | Resource conflict | Attempting to create a user with an existing username. |
| `500 Internal Server Error` | Server-side error | Failure to load or save application data. |

### 4.2 Validation Errors

The API validates incoming requests before processing them.

Examples of validation errors include:

- Missing or blank `userName` parameters.
- Invalid JSON request bodies.
- Missing required JSON fields.
- Incorrect field types.
- Invalid ingredient or food item quantities.
- Unsupported measurement units.
- Blank recipe preparation steps.

Invalid requests generally result in HTTP `400 Bad Request`.

### 4.3 Resource Errors

Some requests may fail because the requested resource does not exist or conflicts with existing data.

Examples:

- `GET /users` returns `404 Not Found` when the specified user does not exist.
- `POST /users` returns `409 Conflict` when the username is already registered.

### 4.4 Persistence Errors

The REST API uses file-based JSON persistence.

If an error occurs while reading or writing the data files, the relevant endpoint returns HTTP `500 Internal Server Error`.

Examples of persistence error messages include:

- `Could not load recipes`
- `Could not save recipe`
- `Could not load fridge`
- `Could not save fridge`
- `Could not load user`
- `Could not save user`

These messages describe the error conditions handled by the controllers. The exact HTTP error response format may depend on Spring Boot's exception handling.

### 4.5 Handling Errors in API Clients

Applications consuming the REST API should:

1. Check the HTTP status code of each response.
2. Validate request parameters and JSON data before sending requests.
3. Handle unsuccessful responses without assuming that the response body contains JSON.
4. Display meaningful error messages to users.
5. Avoid repeatedly submitting requests that have failed due to invalid input.


## 5. Data Persistence and Configuration

### 5.1 Persistence Configuration

Dinnertracker uses JSON files for persistent storage of recipes, fridge contents and users.

The REST API accesses these files through the application's persistence layer.

The configuration is defined in:

`rest/src/main/resources/application.properties`

**Current configuration:**

```properties
server.port=7070
dinnertracker.recipes.path=../data/recipes.json
dinnertracker.fridges.path=../data/fridges.json
dinnertracker.users.path=../data/users.json
```

**Configuration properties:**

| Property | Description |
|---|---|
| `server.port` | Defines the port on which the Spring Boot server listens. |
| `dinnertracker.recipes.path` | Defines the location of the JSON file used for recipe persistence. |
| `dinnertracker.fridges.path` | Defines the location of the JSON file used for fridge persistence. |
| `dinnertracker.users.path` | Defines the location of the JSON file used for user persistence. |

**Persistence components:**

The REST controllers use the following persistence classes:

- `RecipeFileHandler` for loading and saving recipes.
- `FridgeFileHandler` for loading and saving fridge contents.
- `UsersFileHandler` for loading and saving users.

**Relative file paths:**

The configured paths are relative to the application's working directory.

For example, when Spring Boot is started from the `dinnertracker/rest` directory, the path `../data/recipes.json` refers to `dinnertracker/data/recipes.json`.

Starting the application from another working directory may cause the relative paths to resolve to different locations.

**Important:**

The JSON files contain application data. Developers should avoid modifying the project's existing data files when performing manual API tests.

Separate test files should be used for requests that create or update persistent data.

### 5.2 Safe Testing with Isolated Data

Dinnertracker provides a separate Spring Boot configuration for automated tests to help prevent test operations from modifying the application's regular JSON data files.

**Test configuration file:**

`rest/src/test/resources/application-test.properties`

**Current test configuration:**

```properties
dinnertracker.recipes.path=target/recipes-spring-automated-test.json
dinnertracker.fridges.path=target/fridges-spring-automated-test.json
dinnertracker.users.path=target/users-spring-automated-test.json
```

These properties specify separate storage locations for recipes, fridges and users when the Spring Boot `test` profile is active.

#### Automated Testing

Automated tests should use isolated test data rather than the application's regular persistence files.

Using separate test files helps prevent automated test operations from modifying the project's existing recipes, fridge contents and users.

The test configuration defines the file paths. Test classes must activate the appropriate Spring profile and prepare their test data as required.

#### Manual API Testing

Manual requests sent using tools such as `curl` may modify persistent data.

The following endpoints write data:

- `POST /recipes`
- `PUT /fridges`
- `POST /users`

When the server runs with its default configuration, these endpoints use the regular persistence paths defined in `application.properties`.

Developers should therefore avoid sending data-modifying requests against the regular application files unless the changes are intentional.

For manual testing, configure separate JSON file paths and verify that the application is using those paths before sending requests.

#### Important Considerations

- The test configuration does not automatically apply to a normally started server.
- Relative file paths are resolved against the application's working directory.
- Test files should be kept separate from important application data.
- The existence of separate test paths does not, by itself, guarantee that every automated test activates the `test` profile.
- Avoid overwriting existing JSON files during manual testing.


## 6. Testing and Verification

### 6.1 Automated REST API Tests

Dinnertracker includes automated tests for its three REST controllers.

The test classes are located in:

`rest/src/test/java/no/gr2631/dinnertracker/rest/`

**REST test classes:**

| Test class | Resource | Number of test methods |
|---|---|---|
| `RecipeRestControllerTest` | Recipes | 14 |
| `FridgeRestControllerTest` | Fridges | 11 |
| `UserRestControllerTest` | Users | 5 |
| **Total** | | **30** |

**Spring Boot test configuration:**

All three test classes use the following annotations:

```java
@SpringBootTest
@ActiveProfiles("test")
```

`@SpringBootTest` enables testing with the Spring Boot application context.

`@ActiveProfiles("test")` activates the Spring Boot test profile, allowing the tests to use the separate persistence paths configured in `application-test.properties`.

Each test class also uses `@BeforeEach` to execute setup code before individual test methods.

**Test isolation:**

The test profile configures separate JSON files for recipes, fridges and users.

This helps prevent automated REST tests from modifying the regular application data files.

The tests should prepare their required test data independently so that results do not depend on existing application data.


### 6.2 Running Automated Tests with Maven

The automated REST API tests can be executed using Apache Maven.

**Prerequisites:**

- Java 17
- Apache Maven
- A local copy of the Dinnertracker repository

**Running all REST controller tests:**

Open a terminal in the `dinnertracker` directory containing the parent `pom.xml`.

Execute:

```bash
mvn -pl rest -am test -Dtest=RecipeRestControllerTest,FridgeRestControllerTest,UserRestControllerTest -Dsurefire.failIfNoSpecifiedTests=false
```

**Command explanation:**

| Option | Description |
|---|---|
| `-pl rest` | Selects the REST module. |
| `-am` | Also builds the modules required by REST. |
| `test` | Executes Maven's test phase. |
| `-Dtest=...` | Selects the three REST controller test classes. |
| `-Dsurefire.failIfNoSpecifiedTests=false` | Prevents dependent modules from failing when they do not contain the selected tests. |

**Verified test results:**

The command was successfully executed on November 4, 2026.

| Test class | Tests executed | Failures |
|---|---:|---:|
| `FridgeRestControllerTest` | 11 | 0 |
| `RecipeRestControllerTest` | 14 | 0 |
| `UserRestControllerTest` | 5 | 0 |
| **Total** | **30** | **0** |

Maven reported:

```text
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

**Test configuration:**

All three REST test classes use the Spring Boot `test` profile.

The test configuration specifies separate JSON persistence files under the REST module's `target` directory, helping protect regular application data during automated testing.

**Test reports:**

Maven Surefire generates test reports in:

`rest/target/surefire-reports/`

These reports can be inspected to review individual test results and diagnose failures.

**Important:**

A successful test run verifies the behavior covered by the automated tests. It does not guarantee that every possible API request or failure scenario has been tested.

