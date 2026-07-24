package com.revature.controller;

import java.util.Map;

import com.revature.model.Chef;
import com.revature.service.AuthenticationService;
import com.revature.service.ChefService;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * The AuthenticationController class handles user authentication-related
 * operations. This includes login, logout, registration, and managing the
 * authorization filter.
 *
 * It interacts with the ChefService and AuthenticationService for certain
 * functionalities related to the user.
 */
public class AuthenticationController {

    /** A service that handles chef-related operations. */
    private ChefService chefService;
    /** A service that handles authentication-related operations. */
    private AuthenticationService authService;

    /**
     * Constructs an AuthenticationController with the parameters.
     *
     * @param chefService the service used to manage chef-related operations
     *
     * @param authService the service used to manage authentication-related
     *                    operations
     */
    public AuthenticationController(ChefService chefService, AuthenticationService authService) {
        this.chefService = chefService;
        this.authService = authService;
    }

    /**
     * Registers a new chef in the system.
     *
     * If the username already exists, responds with a 409 Conflict status and a
     * result of "Username already exists".
     *
     * Otherwise, registers the chef and responds with a 201 Created status and the
     * registered chef details.
     *
     * @param ctx the Javalin context containing the chef information in the request
     *            body
     */
    public void register(Context ctx) {
        Chef newChef = ctx.bodyAsClass(Chef.class);

        if (chefService.searchChefs(newChef.getUsername()).stream()
                .anyMatch(c -> c.getUsername().equals(newChef.getUsername()))) {
            ctx.status(409).result("Username already exists");
            return;
        }

        Chef registeredChef = authService.registerChef(newChef);
        ctx.status(201).json(registeredChef);
    }

    /**
     * Authenticates a chef and uses a generated authorization token if the
     * credentials are valid. The token is used to check if login is successful. If
     * so, this method responds with a 200 OK status, the token in the response
     * body, and an "Authorization" header that sends the token.
     *
     * If login fails, responds with a 401 Unauthorized status and an error message
     * of "Invalid username or password".
     *
     * @param ctx the Javalin context containing the chef login credentials in the
     *            request body
     */

    public void login(Context ctx) {
        Chef chefCredentials = ctx.bodyAsClass(Chef.class);
        String token = authService.login(chefCredentials);

        if (token != null) {
            boolean isAdmin = chefCredentials.getUsername().equalsIgnoreCase("ChefTrevin");

            String accept = ctx.header("Accept");
            if (accept == null)
                accept = "text/plain";

            if (accept.contains("application/json")) {
                ctx.json(Map.of("token", token, "isAdmin", isAdmin));
            } else {
                ctx.result(token + " " + isAdmin);
            }
        } else {
            ctx.status(401).result("Invalid username or password");
        }
    }

    /**
     * Logs out the currently authenticated chef by invalidating their token.
     * If successful, responds with a 200 OK status and a result of "Logout
     * successful".
     *
     * @param ctx the Javalin context, containing the Authorization token in the
     *            request header
     */
    public void logout(Context ctx) {
        authService.logout(ctx.header("Authorization").split(" ")[1]);

        if (" " != null) {
            ctx.status(200).result("Logout successful");
        }
    }

    /**
     * Configures the routes for authentication operations.
     * Sets up routes for registration, login, and logout, and applies the
     * authorization filter to protect specific routes.
     *
     * @param app the Javalin application to which routes are added
     */
    public void configureRoutes(Javalin app) {
        app.post("/register", this::register);
        app.post("/login", this::login);
        app.post("/logout", this::logout);
    }
}
