package com.revature.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.revature.model.Chef;

/**
 * The AuthenticationService class handles user authentication, including login,
 * logout, registration, and session management for users.
 *
 * This class utilizes the ChefService to interact with chef data.
 */
public class AuthenticationService {

    /**
     * The service used for managing Chef objects and their operations.
     */

    private ChefService chefService;

    /**
     * A map that keeps track of currently logged in users, indexed by session
     * token.
     */
    public static Map<String, Chef> loggedInUsers = new HashMap<>();

    /**
     * Constructs an AuthenticationService with the specified ChefService.
     *
     * @param chefService the ChefService to be used by this authentication service
     */
    public AuthenticationService(ChefService chefService) {
        this.chefService = chefService;
        loggedInUsers = new HashMap<>();
    }

    /**
     * Authenticates a chef by verifying the provided credentials. If successful, a
     * session token is generated and stored in the logged in users map.
     *
     * @param chef the chef object containing username and password for
     *             authentication
     * @return a unique session token if login is successful, or null if
     *         authentication fails
     */
    public String login(Chef chef) {
        List<Chef> existingChefs = chefService.searchChefs(chef.getUsername());

        for (Chef c : existingChefs) {
            if (c.getUsername().equals(chef.getUsername()) && c.getPassword().equals(chef.getPassword())) {
                String token = UUID.randomUUID().toString();
                // add to map
                loggedInUsers.put(token, c);

                return token;
            }

        }

        return null;
    }

    /**
     * Logs out a chef by removing the associated session token from the session
     * map.
     *
     * @param token the session token to be invalidated
     */
    public void logout(String token) {
        loggedInUsers.remove(token);
    }

    /**
     * Registers a new chef by saving the chef's information using ChefService.
     *
     * @param chef the chef object containing registration details
     * @return the registered chef object
     */
    public Chef registerChef(Chef chef) {
        chefService.saveChef(chef);
        return chef;
    }

    /**
     * Retrieves the chef associated with a specific session token.
     *
     * @param token the session token associated with the chef
     * @return the chef associated with the token, or null if the token is invalid
     */
    public Chef getChefFromSessionToken(String token) {
        return loggedInUsers.get(token);
    }

}
