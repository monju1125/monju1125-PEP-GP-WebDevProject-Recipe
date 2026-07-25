/**
 * This script defines the CRUD operations for Recipe objects in the Recipe Management Application.
 */

const BASE_URL = "http://localhost:8081"; // backend URL

let recipes = [];

// Wait for DOM to fully load before accessing elements
window.addEventListener("DOMContentLoaded", () => {

    /* 
     * TODO: Get references to various DOM elements
     * - Recipe name and instructions fields (add, update, delete)
     * - Recipe list container
     * - Admin link and logout button
     * - Search input
    */
   const addRecipeNameInput = document.getElementById("add-recipe-name-input");
   const addRecipeInstructionsInput = document.getElementById("add-recipe-instructions-input");

   const updateRecipeNameInput = document.getElementById("update-recipe-name-input");
   const updateRecipeInstructionsInput = document.getElementById("update-recipe-instructions-input");

   const deleteRecipeNameInput = document.getElementById("delete-recipe-name-input");


   const recipeListContainer = document.getElementById("recipe-list");

   const adminLink = document.getElementById("admin-link");

   const logoutButton = document.getElementById("logout-button");

   const searchInput = document.getElementById("search-input");

   const addRecipeButton = document.getElementById("add-recipe-submit-input");
   const updateRecipeButton = document.getElementById("update-recipe-submit-input");
   const deleteRecipeButton = document.getElementById("delete-recipe-submit-input");
   const searchButton = document.getElementById("search-button");



    /*
     * TODO: Show logout button if auth-token exists in sessionStorage
     */
    if(sessionStorage.getItem("auth-token")){
        logoutButton.style.display = "inline-block";
    }

    /*
     * TODO: Show admin link if is-admin flag in sessionStorage is "true"
     */
    display.adminLink();

    /*
     * TODO: Attach event handlers
     * - Add recipe button → addRecipe()
     * - Update recipe button → updateRecipe()
     * - Delete recipe button → deleteRecipe()
     * - Search button → searchRecipes()
     * - Logout button → processLogout()
     */
    addRecipeButton.addEventListener("click", addRecipe);
    updateRecipeButton.addEventListener("click", updateRecipe);
    deleteRecipeButton.addEventListener("click", deleteRecipe);
    searchButton.addEventListener("click", searchRecipes);
    logoutButton.addEventListener("click", processLogout);

    /*
     * TODO: On page load, call getRecipes() to populate the list
     */
    getRecipes();
    function getAuthHeaders(includeJson= false){
        const headers = {
        "Authentication" : "Bearer" + sessionStorage.getItem("auth-token")
        };
        if(include.Json){
            headers["Content-Type"] = "application/json";
        }
        return headers;
    }

    function displayAdminLink(){
        const isAdmin = sessionStorage.getItem("is-admin");

        if(isAdmin === "true"){
            adminLink.style.display = "inner-block";
        }else {
            adminLink.style.display = "none";
        }
    }

    function normalizeName(value){
        return value.trim().toLowerCase();
    }

    function findRecipeByName(name){
        const normalizedName = normalizeName(name);
        return recipes.find(recipe => normalizeName(recipe.name) === normalizedName);
    }


    /**
     * TODO: Search Recipes Function
     * - Read search term from input field
     * - Send GET request with name query param
     * - Update the recipe list using refreshRecipeList()
     * - Handle fetch errors and alert user
     */


    async function searchRecipes() {
        // Implement search logic here
        const searchTerm = searchInput.value.trim().toLowerCase();

        if(!searchTerm){
            refreshRecipeList(recipes);
            return;
        }
        const filteredRecipes = recipes.filter(recipe => 
            recipe.name && recipe.name.toLowerCase().includes(searchTerm)
        );

        refreshRecipeList(filteredRecipes);
    }

    /**
     * TODO: Add Recipe Function
     * - Get values from add form inputs
     * - Validate both name and instructions
     * - Send POST request to /recipes
     * - Use Bearer token from sessionStorage
     * - On success: clear inputs, fetch latest recipes, refresh the list
     */
    async function addRecipe() {
        // Implement add logic here
        const name = addRecipeNameInput.value.trim();
        const instructions = addRecipeInstructionsInput.value.trim();

        if(!name || !instructions){
            alert("Please enter both recipe name and instructions.");
            return;
        }

        const requestBody = {
            name,
            instructions
        };
        
        try{
            const response = await fetch(`${BASE_URL}/RECIPES`,{
                method: "POST",
                mode: "cors",
                headers: getAuthHeaders(true),
                body: JSON.stringify(requestBody)
            });

            if(response.ok){
                addRecipeNameInput.value = "";
                addRecipeInstructionsInput.value = "";
                await getRecipes();
            }else if(response.status === 401 || response.status === 403){
                alert ("You are not authorized to add recipes.");
            }else if(response.status === 409){
                alert("A recipe with that name already exists.");
            }else{
                alert("Failed to add recipe");
            }

        }catch(error){
            console.error("Add recipe error:", error);
            alert("Unable to add recipe. Please check your connection and try again.");
        }
        

    /**
     * TODO: Update Recipe Function
     * - Get values from update form inputs
     * - Validate both name and updated instructions
     * - Fetch current recipes to locate the recipe by name
     * - Send PUT request to update it by ID
     * - On success: clear inputs, fetch latest recipes, refresh the list
     */
    async function updateRecipe() {
        // Implement update logic here
    }

    /**
     * TODO: Delete Recipe Function
     * - Get recipe name from delete input
     * - Find matching recipe in list to get its ID
     * - Send DELETE request using recipe ID
     * - On success: refresh the list
     */
    async function deleteRecipe() {
        // Implement delete logic here
    }

    /**
     * TODO: Get Recipes Function
     * - Fetch all recipes from backend
     * - Store in recipes array
     * - Call refreshRecipeList() to display
     */
    async function getRecipes() {
        // Implement get logic here
    }

    /**
     * TODO: Refresh Recipe List Function
     * - Clear current list in DOM
     * - Create <li> elements for each recipe with name + instructions
     * - Append to list container
     */
    function refreshRecipeList() {
        // Implement refresh logic here
    }

    /**
     * TODO: Logout Function
     * - Send POST request to /logout
     * - Use Bearer token from sessionStorage
     * - On success: clear sessionStorage and redirect to login
     * - On failure: alert the user
     */
    async function processLogout() {
        // Implement logout logic here
    }

});
