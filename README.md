# PEP-GP-WebDevProject_Recipe

## Background

This project is a web-based recipe management application with user authentication and admin functionality. It allows users to view, add, update, delete, and search recipes. Admins have additional capabilities to manage ingredients. The frontend is built using HTML, CSS, and JavaScript, and communicates with a backend REST API built in Java.

---

## Getting Started

You will be working on the **frontend** of this application.

### Folder Structure

Your work will primarily be within the following folders:

```
src/main/resources/public/frontend/
├── login/
├── register/
├── recipe/
└── ingredients/
```

### Running the Application

The backend runs on: `http://localhost:8081/`

- **If the backend is running, make sure you are not running the automated tests at the same time.**
- Styling via CSS is optional and will not be graded.

---

## Requirements

---

### 1️⃣ User Registration

📂 Location: `frontend/register/`

Files:

- `register-page.html`
- `register-page.js`

**Requirements:**

- Registration form should include:
  - Username, Email, Password, Repeat Password fields
  - Submit button with ID `register-button`
- JS should:
  - Validate that all inputs are filled
  - Ensure password and repeated password match
  - Create a registration object and send a POST request to `http://localhost:8081/register`
  - On success: redirect to login page
  - On failure: show an alert

---

### 2️⃣ User Login and Logout

📂 Location: `frontend/login/` and `frontend/recipe/`

Files:

- `login-page.html`, `login-page.js`
- `recipe-page.html`, `recipe-page.js`

**Requirements:**

- Login form should include username and password fields
- JS should:

  - Create a login request with `username` and `password`
  - Send a POST request to `http://localhost:8081/login`
  - On success:
    - Parse the response (token and admin flag separated by space)
    - Store token in `sessionStorage` under `auth-token`
    - Store admin flag under `is-admin`
    - Redirect to the recipe page
  - On failure: show an alert

- Logout logic (in `recipe-page.js`) should:
  - Send POST request to `/logout` with auth token
  - Clear `sessionStorage` keys
  - Redirect to login page

---

### 3️⃣ Recipe Management

📂 Location: `frontend/recipe/`

Files:

- `recipe-page.html`
- `recipe-page.js`

**Requirements:**

#### HTML

- Add the following elements:
  - Add Recipe: name input, instructions input, add button (`add-recipe-submit-input`)
  - Update Recipe: name input, new instructions input, update button
  - Delete Recipe: name input, delete button
  - Search bar: input + button
  - Recipe list container (ID: `recipe-list`)
  - Admin-only link (ID: `admin-link`) — initially hidden

#### JavaScript

- On page load, call:

  - `getRecipes()` to load all recipes
  - `displayAdminLink()` to toggle admin section visibility

- Implement:
  - `addRecipe()`: POST `/recipes` with name + instructions
  - `updateRecipe()`: PUT `/recipes/{id}` with new instructions
  - `deleteRecipe()`: DELETE `/recipes/{id}`
  - `searchRecipes()`: filter recipes locally based on name
  - All requests must use:
    ```javascript
    headers: {
      "Authorization": "Bearer " + sessionStorage.getItem("auth-token")
    }
    ```
  - After each action, refresh the list dynamically

---

### 4️⃣ Ingredient Management (Admin Only)

📂 Location: `frontend/ingredients/`

Files:

- `ingredient-page.html`
- `ingredient-page.js`

**Requirements:**

#### HTML

- Add:
  - Add Ingredient: name input + button
  - Delete Ingredient: name input + button
  - Ingredient list container (ID: `ingredient-list`)

#### JavaScript

- On page load, call `getIngredients()`
- Implement:
  - `addIngredient()`: POST `/ingredients` with name
  - `deleteIngredient()`: DELETE `/ingredients/{id}`
  - `refreshIngredientList()`: Populate list from array
  - Use:
    ```javascript
    headers: {
      "Authorization": "Bearer " + sessionStorage.getItem("auth-token")
    }
    ```
  - Token and admin status must be verified via `sessionStorage`

---

## CORS Explanation

CORS (Cross-Origin Resource Sharing) is a security feature implemented by browsers to restrict cross-origin HTTP requests initiated from scripts. Since your frontend and backend run on the same machine but from different contexts (e.g., `file://` for frontend vs `http://localhost:8081` for backend), CORS preflight requests ensure that the backend explicitly allows the frontend to interact with it.

Modern browsers send a preflight `OPTIONS` request before making actual requests like `POST`, `PUT`, or `DELETE`. The backend must respond with the appropriate headers:

```http
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
```

Your MockServer and backend are pre-configured to include these headers for all necessary routes.

---

## Helpful References

- MDN Fetch API Guide:  
  https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch

- MDN CORS Overview:  
  https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS

- JavaScript `fetch()` syntax and examples:  
  https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API

---
