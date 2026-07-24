package com.revature.service;

import java.util.List;
import java.util.Optional;

import com.revature.model.Chef;
import com.revature.dao.ChefDAO;
import com.revature.util.Page;
import com.revature.util.PageOptions;

/**
 * The ChefService class provides business logic for operations related to Chef entities.
 *
 * It interacts with the ChefDAO to perform CRUD operations and search functionality.
 */
public class ChefService {

    /** Data access object for Chef entities. */
    private ChefDAO chefDAO;

    /**
     * Constructs a ChefService with the specified ChefDAO.
     *
     * @param chefDao the ChefDAO used for accessing and managing Chef data
     */
    public ChefService(ChefDAO chefDAO) {
        this.chefDAO = chefDAO;
    }

    /**
     * Finds a Chef by their unique identifier.
     *
     * @param id the unique identifier of the Chef
     * @return an Optional containing the Chef if found, or an empty Optional if not found
     */
    public Optional<Chef> findChef(int id) {
        return Optional.ofNullable(chefDAO.getChefById(id));
    }

    /**
     * Saves a Chef entity. If the Chef's ID is zero, a new Chef is created and the `chef` parameter's ID is updated.
     *
     * Otherwise, updates the existing Chef.
     *
     * @param chef the Chef entity to be saved or updated
     */
    public void saveChef(Chef chef) {
        if (chef.getId() == 0) {
            int id = chefDAO.createChef(chef);
            chef.setId(id);
        } else {
            chefDAO.updateChef(chef);
        }
    }

    /**
     * Searches for Chefs based on a search term.
     * If the term is null, retrieves all Chefs.
     *
     * @param term the search term for filtering Chefs by attributes
     * @return a list of Chefs matching the search criteria, or all Chefs if term is null
     */
    public List<Chef> searchChefs(String term) {
        if (term == null) {
            return chefDAO.getAllChefs();
        } else {
            return chefDAO.searchChefsByTerm(term);
        }
    }

    /**
     * Deletes a Chef based on their unique identifier, if they exist.
     *
     * @param id the unique identifier of the Chef to be deleted
     */
    public void deleteChef(int id) {
        Chef chef = chefDAO.getChefById(id);
        if (chef != null) {
            chefDAO.deleteChef(chef);
        }
    }

    /**
     * Searches for Chefs based on a search term with pagination and sorting options.
     *
     * @param term the search term for filtering Chefs by attributes
     * @param page the page number to retrieve
     * @param pageSize the number of results per page
     * @param sortBy the field to sort the results by
     * @param sortDirection the direction of sorting (e.g., "asc" or "desc")
     *
     * @return a Page object containing the list of Chefs matching the criteria
     */
    public Page<Chef> searchChefs(String term, int page, int pageSize, String sortBy, String sortDirection) {
        PageOptions options = new PageOptions(page, pageSize, sortBy, sortDirection);
        if (term == null ) {
            return chefDAO.getAllChefs(options);
        } else {
            return chefDAO.searchChefsByTerm(term, options);
        }
    }
}
