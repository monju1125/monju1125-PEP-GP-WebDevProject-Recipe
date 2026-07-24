package com.revature.util;

import java.util.stream.Collectors;

import com.revature.dao.ChefDAO;
import com.revature.model.Chef;
import com.revature.service.AuthenticationService;
import com.revature.service.ChefService;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;

/**
 * The AdminMiddleware class is responsible for enforcing access control
 * within the application by protecting specific routes from non-admin users.
 * 
 * This class utilizes a list of protected methods to determine which HTTP
 * methods require admin access and leverages the ChefService to validate
 * user permissions. The middleware intercepts requests and ensures that
 * only users with admin privileges can access protected resources.
 */

public class AdminMiddleware implements Handler {

    /**
     * An array of protected HTTP methods that require admin access.
     */

    private String[] protectedMethods;

    /**
     * The AuthenticationService instance used for handling authentication-related
     * operations and validation.
     */

    private AuthenticationService authService;

    /**
     * Constructs an AdminMiddleware instance with the specified
     * AuthenticationService and an array of protected methods.
     *
     * (FOR REFERENCE) This method is part of the backend logic.
     * No modifications or implementations are required.
     */

    public AdminMiddleware(String... protectedMethods) {
        this.protectedMethods = protectedMethods;
        this.authService = new AuthenticationService(new ChefService(new ChefDAO(new ConnectionUtil())));
    }

    /**
     * Handles the HTTP request, checking for admin access based on the HTTP method
     * being used and the current logged in user's authentication token.
     *
     * (FOR REFERENCE) This method is part of the backend logic.
     * No modifications or implementations are required.
     */
    @Override
    public void handle(Context ctx) {
        if (isProtectedMethod(ctx.method().name())) {
            // Get the token of the current logged in user
            String token = AuthenticationService.loggedInUsers.keySet().stream().collect(Collectors.joining());

            // Check the corresponding chef and check if they are admin
            boolean isAdmin = isAdmin(authService.getChefFromSessionToken(token));

            // If they are not admin, throw an exception
            if (!isAdmin) {
                throw new UnauthorizedResponse("Access denied");
            }
        }
    }

    /**
     * Checks if the specified HTTP method is among the protected methods.
     *
     * (FOR REFERENCE) This method is part of the backend logic.
     * No modifications or implementations are required.
     */
    private boolean isProtectedMethod(String method) {
        for (String protectedMethod : protectedMethods) {
            if (protectedMethod.toString().equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the chef with the specified ID has admin privileges.
     *
     * (FOR REFERENCE) This method is part of the backend logic.
     * No modifications or implementations are required.
     */
    private boolean isAdmin(Chef chef) {
        if (chef != null) {
            return chef.isAdmin();
        }
        return false;
    }
}
