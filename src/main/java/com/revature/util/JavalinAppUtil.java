package com.revature.util;
import com.revature.controller.RecipeController;

import io.javalin.Javalin;

import com.revature.controller.AuthenticationController;
import com.revature.controller.IngredientController;


/**
 * The JavalinAppUtil class is responsible for setting up and configuring 
 * the Javalin application instance. This utility class encapsulates the 
 * necessary controllers for handling different aspects of the application, 
 * such as recipes, authentication, and ingredients. It provides a method 
 * to create and configure the Javalin app instance, including defining 
 * the routes for each controller and applying any necessary middleware, 
 * such as admin middleware.
 */

public class JavalinAppUtil {

    /**
     * The RecipeController for handling recipe-related routes.
     */

    private RecipeController recipeController;

    /**
     * The AuthenticationController for handling authentication-related routes.
     */

    private AuthenticationController authenticationController;

    /**
     * The IngredientController for handling ingredient-related routes.
     */

    private IngredientController ingredientController;

    /**
     * Constructs a JavalinAppUtil with the specified controllers.
     *
     * @param recipeController the controller for handling recipe operations
     * @param authController the controller for handling authentication operations
     * @param ingredientController the controller for handling ingredient operations
     */

    public JavalinAppUtil(RecipeController recipeController, AuthenticationController authController, IngredientController ingredientController) {
        this.recipeController = recipeController;
        this.authenticationController = authController;
        this.ingredientController = ingredientController;
    }

    /**
     * Creates a Javalin instance, configures the routes for all controllers, 
     * and applies any necessary middleware, including admin middleware.
     *
	 * (FOR REFERENCE) This method is part of the backend logic.
     * No modifications or implementations are required.
     */
    public Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });

            });

            
        });


        // Configure routes for each controller
        recipeController.configureRoutes(app);
        authenticationController.configureRoutes(app);
        ingredientController.configureRoutes(app);

        app.before("/recipes/*", new AdminMiddleware("DELETE"));
        app.before("/ingredients/*", new AdminMiddleware("UPDATE", "CREATE", "DELETE"));

        return app;
    }


}
