package com.revature.service;

import java.util.List;
import java.util.Optional;

import com.revature.model.Ingredient;
import com.revature.dao.IngredientDAO;
import com.revature.util.Page;
import com.revature.util.PageOptions;

/**
 * The IngredientService class provides business logic for operations related to Ingredient entities.
 *
 * It interacts with the IngredientDAO to perform CRUD operations and search functionality.
 */
public class IngredientService {

    /** Data access object for Ingredient entities. */
    private IngredientDAO ingredientDAO;

    /**
     * Constructs an IngredientService with the specified IngredientDAO.
     *
     * @param ingredientDAO the IngredientDAO used for accessing and managing Ingredient data
     */
    public IngredientService(IngredientDAO ingredientDAO) {
        this.ingredientDAO = ingredientDAO;
    }

    /**
     * Finds an Ingredient by its unique identifier.
     *
     * @param id the unique identifier of the Ingredient
     * @return an Optional containing the Ingredient if found, or an empty Optional if not found
     */
    public Optional<Ingredient> findIngredient(int id) {
        return Optional.ofNullable(ingredientDAO.getIngredientById(id));
    }

    /**
     * Saves an Ingredient entity. If the Ingredient's ID is zero, a new Ingredient is created and the `ingredient` parameter's ID is updated.
     * Otherwise, updates the existing Ingredient.
     *
     * @param ingredient the Ingredient entity to be saved or updated
     */
    public void saveIngredient(Ingredient ingredient) {
        if(ingredient.getId() == 0) {
            int id = ingredientDAO.createIngredient(ingredient);
            ingredient.setId(id);
        } else {
            ingredientDAO.updateIngredient(ingredient);
        }
    }

    /**
     * Deletes an Ingredient based on its unique identifier, if it exists.
     *
     * @param id the unique identifier of the Ingredient to be deleted
     */
    public void deleteIngredient(int id) {
        Ingredient ingredient = ingredientDAO.getIngredientById(id);
        if(ingredient != null) {
            ingredientDAO.deleteIngredient(ingredient);
        }
    }

    /**
     * Searches for Ingredients based on a search term.
     * If the term is null, retrieves all Ingredients.
     *
     * @param term the search term for filtering Ingredients by attributes
     * @return a list of Ingredients matching the search criteria, or all Ingredients if term is null
     */
    public List<Ingredient> searchIngredients(String term) {
        if(term == null ) {
            return ingredientDAO.getAllIngredients();
        } else {
            return ingredientDAO.searchIngredients(term);
        }
    }

    /**
     * Searches for Ingredients based on a search term with pagination and sorting options.
     *
     * @param term the search term for filtering Ingredients by attributes
     * @param page the page number to retrieve
     * @param pageSize the number of results per page
     * @param sortBy the field to sort the results by
     * @param sortDirection the direction of sorting (e.g., "asc" or "desc")
     * @return a Page object containing the list of Ingredients matching the criteria
     */
    public Page<Ingredient> searchIngredients(String term, int page, int pageSize, String sortBy, String sortDirection) {
        PageOptions pageOptions = new PageOptions(page, pageSize, sortBy, sortDirection);
        if(term == null) {
            return ingredientDAO.getAllIngredients(pageOptions);
        } else {
            return ingredientDAO.searchIngredients(term, pageOptions);
        }
    }
}
