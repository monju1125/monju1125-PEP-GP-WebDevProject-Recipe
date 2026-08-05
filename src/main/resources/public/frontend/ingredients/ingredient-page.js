/**
 * This script defines the add, view, and delete operations for Ingredient objects in the Recipe Management Application.
 */

const BASE_URL = "http://localhost:8081"; // backend URL


/* 
 * TODO: Get references to various DOM elements
 * - addIngredientNameInput
 * - deleteIngredientNameInput
 * - ingredientListContainer
 * - searchInput (optional for future use)
 * - adminLink (if visible conditionally)
 */

const addIngredientNameInput = document.getElementById("add-ingredient-name-input");
const deleteIngredientNameInput = document.getElementById("delete-ingredient-name-input");
const ingredientListContainer = document.getElementById("ingredient-list");
const addIngredientSubmitButton = document.getElementById("add-ingredient-submit-button");
const deleteIngredientSubmitButton = document.getElementById("delete-ingredient-submit-button");

let ingredients = [];

if(!sessionStorage.getItem("auth-token")){
    console.error("Please login first.");
    window.location.href= "../login/login-page.html";
}

if(sessionStorage.getItem("is-admin") !== "true"){
    alert("Admin access is required to manage ingredients. ");
    window.location.href = "../recipe/recipe-page.html";
}

/* 
 * TODO: Attach 'onclick' events to:
 * - "add-ingredient-submit-button" → addIngredient()
 * - "delete-ingredient-submit-button" → deleteIngredient()
 */
if(addIngredientSubmitButton){
    addIngredientSubmitButton.addEventListener("click", addIngredient);
}

/*
 * TODO: Create an array to keep track of ingredients
 */
if(deleteIngredientSubmitButton){
    deleteIngredientSubmitButton.addEventListener("click", deleteIngredient);
}

/* 
 * TODO: On page load, call getIngredients()
 */
window.addEventListener("DOMContentLoaded", getIngredients);

/**
 * TODO: Add Ingredient Function
 * 
 * Requirements:
 * - Read and trim value from addIngredientNameInput
 * - Validate input is not empty
 * - Send POST request to /ingredients
 * - Include Authorization token from sessionStorage
 * - On success: clear input, call getIngredients() and refreshIngredientList()
 * - On failure: alert the user
 */
function getAuthHeaders(includeJson = false){
    const headers = {
        "Authorization" : "Bearer " + sessionStorage.getItem("auth-token")
    };

    if(includeJson){
        headers["Content-Type"] = "application/json";
    }
    return headers;
}
function normalizeName(value){
    return value.trim().toLowerCase();
}

function findIngredientByName(name){
    const normalizedName = normalizeName(name);
    return ingredients.find(ingredient => 
        normalizeName(ingredient.name) === normalizedName);
}

async function addIngredient() {
    // Implement add ingredient logic here
    const name = addIngredientNameInput.value.trim();

    if(!name){
        alert("Please enter an ingredient name.");
        return;
    }

    const requestBody = {
        name
    };

    try{
        const response = await fetch(`${BASE_URL}/ingredients`,{
            method: "POST",
            mode: "cors",
            headers: getAuthHeaders(true),
            body: JSON.stringify(requestBody)
        });

        if(response.ok){
            addIngredientNameInput.value = "";
            await getIngredients(); 
        }else if(response.status === 401 || response.status === 403){
            console.error("You are not authorized to add ingredients.");
        }else if(response.status === 409){
            alert("Ingredients already exists.");
        }else{
            alert("Failed to add ingredients");
        }
    }catch(error){
        console.error("Add ingredient error");
        alert("Unable to add ingredient. Please check your connection and try it again");
    }
}


/**
 * TODO: Get Ingredients Function
 * 
 * Requirements:
 * - Fetch all ingredients from backend
 * - Store result in `ingredients` array
 * - Call refreshIngredientList() to display them
 * - On error: alert the user
 */
async function getIngredients() {
    // Implement get ingredients logic here
    try{
        const response = await fetch(`${BASE_URL}/ingredients`,{
            method: "GET",
            mode: "cors",
            headers: getAuthHeaders()
        });
        if(response.ok){
            ingredients = await response.json();
            refreshIngredientList();
        }else if(response.status === 401 || response.status === 403){
            console.error("You are not authorized to view ingredients.");
            window.location.href = "../recipe/recipe-page.html";
        }else{
            console.error("Failed to fetch ingredients.");
        }
    }catch(error){
        console.error("Get ingredients error:", error);
        alert("Unable to fetch ingredients. Please check your connection and try again.");

    }
}


/**
 * TODO: Delete Ingredient Function
 * 
 * Requirements:
 * - Read and trim value from deleteIngredientNameInput
 * - Search ingredientListContainer's <li> elements for matching name
 * - Determine ID based on index (or other backend logic)
 * - Send DELETE request to /ingredients/{id}
 * - On success: call getIngredients() and refreshIngredientList(), clear input
 * - On failure or not found: alert the user
 */
async function deleteIngredient() {
    // Implement delete ingredient logic here
    const name = deleteIngredientNameInput.value.trim();

    if(!name){
        alert("Please enter an ingredient name to delete.");
        return;
    }
    await getIngredients();

    const ingredientToDelete = findIngredientByName(name);

    if(!ingredientToDelete){
        alert("Ingredient not found.");
        return;
    }

    try{
        const response = await fetch(`${BASE_URL}/ingredients/${ingredientToDelete.id}`,{
            method: "DELETE",
            mode: "cors",
            headers: getAuthHeaders()
        });

        if(response.ok){
            deleteIngredientNameInput.value = "";
            ingredients = ingredients.filter(ingredient => ingredient.id != ingredientToDelete.id);
            refreshIngredientList();
            //await getIngredients();
        }else if(response.status === 401 || response.status === 403){
            console.error("You are not authorized to delete ingredients.");
        }else{
            console.error("Failed to delete ingredient.");
        }
    }catch(error){
        console.error("Delete ingredient error:", error);
        //alert("Unable to delete ingredient. Please check your connection and try again.");
    }
}


/**
 * TODO: Refresh Ingredient List Function
 * 
 * Requirements:
 * - Clear ingredientListContainer
 * - Loop through `ingredients` array
 * - For each ingredient:
 *   - Create <li> and inner <p> with ingredient name
 *   - Append to container
 */
function refreshIngredientList() {
    // Implement ingredient list rendering logic here
    ingredientListContainer.innerHTML = "";

    if(!ingredients || ingredients.length === 0){
        const emptyItem = document.createElement("li");
        emptyItem.textContent = "No ingredients found";
        ingredientListContainer.appendChild(emptyItem);
        return;
    }

    ingredients.forEach(ingredient => {
        const listItem = document.createElement("li");

        const nameParagraph = document.createElement("p");
        //nameParagraph.textContent = ingredient.name;
        listItem.textContent = ingredient.name;

        listItem.appendChild(nameParagraph);
        ingredientListContainer.appendChild(listItem);
    });
}
