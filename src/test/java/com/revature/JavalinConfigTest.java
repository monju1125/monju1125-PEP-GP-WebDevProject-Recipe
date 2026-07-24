package com.revature;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import com.revature.util.ConnectionUtil;
import com.revature.util.JavalinAppUtil;

public class JavalinConfigTest {

	private RecipeDAO recipeDao;
	private RecipeService recipeService;
	private RecipeController recipeController;
	private ChefDAO chefDao;
	private ChefService chefService;
	private AuthenticationService authService;
	private AuthenticationController authController;
	private IngredientDAO ingredientDao;
	private IngredientService ingredientService;
	private IngredientController ingredientController;

	@BeforeEach
	void setUpTestsData() throws SQLException {

		chefDao = new ChefDAO(new ConnectionUtil());
		chefService = new ChefService(chefDao);
		authService = new AuthenticationService(chefService);
		authController = new AuthenticationController(chefService, authService);

		ingredientDao = new IngredientDAO(new ConnectionUtil());
		ingredientService = new IngredientService(ingredientDao);
		ingredientController = new IngredientController(ingredientService);

		recipeDao = new RecipeDAO(chefDao, ingredientDao, new ConnectionUtil());
		recipeService = new RecipeService(recipeDao);
		recipeController = new RecipeController(recipeService, authService);
	}

	@Test
	public void test() {

		new JavalinAppUtil(recipeController, authController, ingredientController).getApp().start();

	}

}
