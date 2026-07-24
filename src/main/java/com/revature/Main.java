package com.revature;

import com.revature.controller.AuthenticationController;
import com.revature.controller.IngredientController;
import com.revature.controller.RecipeController;
import com.revature.dao.ChefDAO;
import com.revature.dao.IngredientDAO;
import com.revature.dao.RecipeDAO;
import com.revature.service.AuthenticationService;
import com.revature.service.ChefService;
import com.revature.service.IngredientService;
import com.revature.service.RecipeService;
import com.revature.util.AdminMiddleware;
import com.revature.util.ConnectionUtil;
import com.revature.util.DBUtil;
import com.revature.util.JavalinAppUtil;

import io.javalin.Javalin;

/**
 * Allows manual or automated startup of the backend server.
 * Manual: runs on 8081
 * Tests: call startServer(8081, true) to auto-assign free port.
 */
public class Main {

    private static final ConnectionUtil CONNECTION_UTIL = new ConnectionUtil();
    private static JavalinAppUtil JAVALIN_APP_UTIL;
    private static RecipeController RECIPE_CONTROLLER;
    private static RecipeService RECIPE_SERVICE;
    private static RecipeDAO RECIPE_DAO;
    private static ChefDAO CHEF_DAO;
    private static ChefService CHEF_SERVICE;
    private static AuthenticationService AUTH_SERVICE;
    private static AuthenticationController AUTH_CONTROLLER;
    private static IngredientDAO INGREDIENT_DAO;
    private static IngredientService INGREDIENT_SERVICE;
    private static IngredientController INGREDIENT_CONTROLLER;
    @SuppressWarnings("unused")
    private static AdminMiddleware ADMIN_MIDDLEWARE;

    public static void main(String[] args) {
        startServer(8081, true);
    }

    public static Javalin startServer(int preferredPort, boolean allowFallback) {

        // === Initialize dependencies ===
        INGREDIENT_DAO = new IngredientDAO(CONNECTION_UTIL);
        CHEF_DAO = new ChefDAO(CONNECTION_UTIL);
        RECIPE_DAO = new RecipeDAO(CHEF_DAO, INGREDIENT_DAO, CONNECTION_UTIL);
        CHEF_SERVICE = new ChefService(CHEF_DAO);
        AUTH_SERVICE = new AuthenticationService(CHEF_SERVICE);
        RECIPE_SERVICE = new RecipeService(RECIPE_DAO);
        RECIPE_CONTROLLER = new RecipeController(RECIPE_SERVICE, AUTH_SERVICE);
        INGREDIENT_SERVICE = new IngredientService(INGREDIENT_DAO);
        INGREDIENT_CONTROLLER = new IngredientController(INGREDIENT_SERVICE);
        AUTH_CONTROLLER = new AuthenticationController(CHEF_SERVICE, AUTH_SERVICE);
        JAVALIN_APP_UTIL = new JavalinAppUtil(RECIPE_CONTROLLER, AUTH_CONTROLLER, INGREDIENT_CONTROLLER);

        // Run any DB init scripts
        DBUtil.RUN_SQL();

        Javalin app = JAVALIN_APP_UTIL.getApp();

        int port = preferredPort;
        while (true) {
            try {
                app.start(port);
                System.out.println("Server started on port: " + port);
                break;
            } catch (Exception e) {
                if (allowFallback && e.getMessage() != null && e.getMessage().contains("Address already in use")) {
                    port++;
                    System.out.println("Port " + (port - 1) + " busy, retrying on " + port);
                } else {
                    throw e;
                }
            }
        }

        return app;
    }
}
