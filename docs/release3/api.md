# Dinnertracker REST API

Dinnertracker exposes a Spring Boot REST API for recipes, fridge contents and users. JSON is used for structured requests and responses unless noted otherwise.

**Base URL (local):** `http://localhost:7070`

## 1. Run locally

Requires Java 17 and Apache Maven.

From the `dinnertracker` directory (containing the parent `pom.xml`):

```bash
mvn -pl rest -am install -DskipTests
cd rest
mvn spring-boot:run
```

Check the server with `curl -i http://localhost:7070/health`; stop it with `Ctrl+C`.

## 2. Endpoint overview

**Data safety:** `POST /recipes`, `PUT /fridges` and `POST /users` modify persistent JSON files. Use separate persistence files when testing these endpoints manually.

| Method | Path | Purpose |
|---|---|---|
| GET | `/health` | Check server availability |
| GET | `/recipes?userName=...` | List one user's recipes |
| GET | `/recipes/all` | List all recipes |
| POST | `/recipes` | Create a recipe |
| GET | `/fridges?userName=...` | Get one user's fridge |
| PUT | `/fridges?userName=...` | Replace one user's fridge contents |
| GET | `/users?userName=...` | Find a user |
| POST | `/users` | Create a user |

## 3. Endpoint reference

Examples use illustrative data. `userName` query parameters must be non-blank where required.

### GET /health

```bash
curl -i http://localhost:7070/health
```

**200 OK** — plain-text response: `Dinnertracker REST API is running`.

### GET /recipes

Requires `userName`. Returns a JSON array (possibly empty).

```bash
curl -i 'http://localhost:7070/recipes?userName=TestUser'
```

**200 OK** — example:

```json
[
  {
    "userName": "TestUser",
    "name": "Pancakes",
    "ingredients": [{"name": "Flour", "quantity": 2.0, "unit": "KILOGRAM"}],
    "steps": ["Mix the ingredients", "Cook the pancakes"]
  }
]
```

| Status | Meaning |
|---|---|
| 200 | Recipes retrieved |
| 400 | Missing or blank `userName` |
| 500 | Unable to load recipes |

### GET /recipes/all

Returns recipes across all users; no query parameter is required. An empty database produces `[]`.

```bash
curl -i http://localhost:7070/recipes/all
```

**200 OK** — example:

```json
[
  {"userName":"TestUser","name":"Pancakes","ingredients":[],"steps":[]},
  {"userName":"AnotherUser","name":"Pizza","ingredients":[],"steps":[]}
]
```

| Status | Meaning |
|---|---|
| 200 | Recipes retrieved |
| 500 | Unable to load recipes |

### POST /recipes

Creates a recipe. `userName` and `name` must be non-blank. `ingredients` and `steps` are optional arrays; ingredient names must be non-blank, quantities positive and units supported by the `Unit` enum. Steps cannot be blank.

**JSON request:**

```json
{
  "userName": "TestUser",
  "name": "Pancakes",
  "ingredients": [{"name": "Flour", "quantity": 2.0, "unit": "KILOGRAM"}],
  "steps": ["Mix the ingredients", "Cook the pancakes"]
}
```

**curl (Git Bash):**

```bash
curl -i -X POST http://localhost:7070/recipes \
  -H 'Content-Type: application/json' \
  -d '{"userName":"TestUser","name":"Pancakes","ingredients":[{"name":"Flour","quantity":2.0,"unit":"KILOGRAM"}],"steps":["Mix the ingredients","Cook the pancakes"]}'
```

**201 Created** — plain-text response: `Recipe created successfully`.

| Status | Meaning |
|---|---|
| 201 | Recipe saved |
| 400 | Invalid JSON or recipe data |
| 500 | Unable to load or save recipes |

### GET /fridges

Requires `userName`. Returns a JSON object containing `userName` and `foodItems` (which may be empty).

```bash
curl -i 'http://localhost:7070/fridges?userName=TestUser'
```

**200 OK** — example:

```json
{
  "userName": "TestUser",
  "foodItems": [
    {"name": "Milk", "quantity": 2.0, "unit": "LITRE"},
    {"name": "Flour", "quantity": 1.0, "unit": "KILOGRAM"}
  ]
}
```

| Status | Meaning |
|---|---|
| 200 | Fridge retrieved |
| 400 | Missing or blank `userName` |
| 500 | Unable to load fridge |

### PUT /fridges

Requires `userName` and a JSON object with a `foodItems` array. **Replaces** existing contents rather than appending items; the array may be empty. Each item needs a non-blank `name`, a finite positive `quantity` and a supported `unit`.

**JSON request:**

```json
{
  "foodItems": [
    {"name": "Milk", "quantity": 2.0, "unit": "LITRE"},
    {"name": "Flour", "quantity": 1.0, "unit": "KILOGRAM"}
  ]
}
```

**curl (Git Bash):**

```bash
curl -i -X PUT 'http://localhost:7070/fridges?userName=TestUser' \
  -H 'Content-Type: application/json' \
  -d '{"foodItems":[{"name":"Milk","quantity":2.0,"unit":"LITRE"},{"name":"Flour","quantity":1.0,"unit":"KILOGRAM"}]}'
```

**200 OK** — plain-text response: `Fridge updated successfully`.

| Status | Meaning |
|---|---|
| 200 | Fridge updated |
| 400 | Missing username or invalid JSON/food items |
| 500 | Unable to save fridge |

### GET /users

Requires `userName`. A found user's name is returned as **plain text**, not JSON.

```bash
curl -i 'http://localhost:7070/users?userName=TestUser'
```

**200 OK** — response body: `TestUser`.

| Status | Meaning |
|---|---|
| 200 | User found |
| 400 | Missing or blank `userName` |
| 404 | User not found |
| 500 | Unable to load users |

### POST /users

Creates a user with a non-blank `userName`; duplicate usernames are rejected.

**JSON request:**

```json
{"userName": "TestUser"}
```

**curl (Git Bash):**

```bash
curl -i -X POST http://localhost:7070/users \
  -H 'Content-Type: application/json' \
  -d '{"userName":"TestUser"}'
```

**201 Created** — plain-text response: `User created successfully`.

| Status | Meaning |
|---|---|
| 201 | User created |
| 400 | Invalid JSON or username |
| 409 | Username already exists |
| 500 | Unable to save user |

## 4. Persistence and test data

The application stores recipes, fridges and users in JSON files. Paths are configured in `rest/src/main/resources/application.properties`:

```properties
server.port=7070
dinnertracker.recipes.path=../data/recipes.json
dinnertracker.fridges.path=../data/fridges.json
dinnertracker.users.path=../data/users.json
```

Paths are relative to the process working directory (the values above assume the server is started from `dinnertracker/rest`). For automated tests, `rest/src/test/resources/application-test.properties` configures separate files under `target/`; the test classes activate that profile with `@ActiveProfiles("test")`. The test profile does **not** apply automatically to a normally started server, so manual tests that write data need separate configured paths.

## 5. Automated REST tests

REST controller tests are in `rest/src/test/java/no/gr2631/dinnertracker/rest/`:

- `RecipeRestControllerTest`
- `FridgeRestControllerTest`
- `UserRestControllerTest`

Run them from the `dinnertracker` directory:

```bash
mvn -pl rest -am test -Dtest=RecipeRestControllerTest,FridgeRestControllerTest,UserRestControllerTest -Dsurefire.failIfNoSpecifiedTests=false
```

Reports are written to `rest/target/surefire-reports/`.
