package com.revature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.controller.AuthenticationController;
import com.revature.controller.IngredientController;
import com.revature.controller.RecipeController;
import com.revature.model.Chef;
import com.revature.model.Recipe;
import com.revature.dao.ChefDAO;
import com.revature.dao.IngredientDAO;
import com.revature.dao.RecipeDAO;
import com.revature.service.AuthenticationService;
import com.revature.service.ChefService;
import com.revature.service.IngredientService;
import com.revature.service.RecipeService;
import com.revature.util.ConnectionUtil;
import com.revature.util.DBUtil;
import com.revature.util.JavalinAppUtil;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginIntegrationTest {

	private static int PORT = 8085;
	private static String BASE_URL = "http://localhost:" + PORT;
	private RecipeDAO recipeDAO;
	private RecipeService recipeService;
	private RecipeController recipeController;
	private ChefDAO chefDAO;
	private ChefService chefService;
	private AuthenticationService authService;
	private AuthenticationController authController;
	private IngredientDAO ingredientDAO;
	private IngredientService ingredientService;
	private IngredientController ingredientController;
	private JavalinAppUtil appUtil;
	private Javalin app;
	private OkHttpClient client;

	@BeforeEach
	void setUpTestsData() throws SQLException {
		DBUtil.RUN_SQL();
		chefDAO = new ChefDAO(new ConnectionUtil());
		ingredientDAO = new IngredientDAO(new ConnectionUtil());
		recipeDAO = new RecipeDAO(chefDAO, ingredientDAO, new ConnectionUtil());
		recipeService = new RecipeService(recipeDAO);
		ingredientService = new IngredientService(ingredientDAO);
		chefService = new ChefService(chefDAO);
		authService = new AuthenticationService(chefService);
		authController = new AuthenticationController(chefService, authService);
		recipeController = new RecipeController(recipeService, authService);
		ingredientController = new IngredientController(ingredientService);
		appUtil = new JavalinAppUtil(recipeController, authController, ingredientController);
		app = appUtil.getApp();
		app.start(PORT);
		client = new OkHttpClient();

	}

	@AfterEach
	void tearDownTestsData() {

		app.stop();

	}

	@Test
	public void testSuccessfulLogin() throws IOException {
		Chef chef = new Chef(1, "JoeCool", "snoopy@null.com", "redbarron", false);
		Recipe newRecipe = new Recipe(6, "fried fish", "fish, oil, stove", chef);

		// Prepare login request body
		RequestBody chefBody = RequestBody.create(
				"{\"username\":\"" + chef.getUsername() + "\",\"password\":\"" + chef.getPassword() + "\"}",
				MediaType.get("application/json; charset=utf-8"));

		// Send login request
		Request loginRequest = new Request.Builder().url(BASE_URL + "/login").post(chefBody).build();
		Response loginResponse = client.newCall(loginRequest).execute();

		// Retrieve response body once
		String responseBodyString = loginResponse.body() != null ? loginResponse.body().string() : null;

		// Verify login response
		assertEquals(200, loginResponse.code(), "Login failed: " + responseBodyString);
		// Retrieve token from response
		String token = responseBodyString; // If your response body is just the token

		// Prepare recipe request body
		RequestBody recipeBody = RequestBody.create(new JavalinJackson().toJsonString(newRecipe, Recipe.class),
				MediaType.get("application/json; charset=utf-8"));

		// Send recipe creation request
		Request recipeRequest = new Request.Builder()
				.url("http://localhost:8085/recipes")
				.addHeader("Authorization", "Bearer " + token) // Ensure correct format
				.post(recipeBody)
				.build();

		Response postResponse = client.newCall(recipeRequest).execute();

		// Verify recipe creation response
		assertEquals(201, postResponse.code(),
				"Expected: 201, Actual: " + postResponse.code() + ", Response: "
						+ (postResponse.body() != null ? postResponse.body().string() + "hello" : "null"));

		// Verify that the recipe can be retrieved
		Request getRequest = new Request.Builder()
				.url(BASE_URL + "/recipes/6")
				.addHeader("Authorization", "Bearer " + token) // Ensure correct format
				.get()
				.build();

		Response getResponse = client.newCall(getRequest).execute();
		String rBody = getResponse.body().string();
		assertEquals(200, getResponse.code(), "Expected: 200, Actual: " + getResponse.code());
		assertEquals(new JavalinJackson().toJsonString(newRecipe, Recipe.class), rBody,
				"Newly created Recipe should be returned as JSON");
	}

	@Test
	public void testUnsuccessfulLogin() throws IOException {

		Chef chef = new Chef(1, "JoeCool", "snoopy@null.com", "woodstock", false);
		Recipe newRecipe = new Recipe(6, "fried fish", "fish, oil, stove", chef);
		RequestBody chefBody = RequestBody.create(
				"{\"username\": \"" + chef.getUsername() + "\", \"password\": \"" + chef.getPassword() + "\"}",
				MediaType.get("application/json; charset=utf-8"));
		Request loginRequest = new Request.Builder().url(BASE_URL + "/login").post(chefBody).build();
		Response loginResponse = client.newCall(loginRequest).execute();
		String token = loginResponse.body().string();
		RequestBody recipeBody = RequestBody.create(new JavalinJackson().toJsonString(newRecipe, Recipe.class),
				MediaType.get("application/json; charset=utf-8"));
		Request recipeRequest = new Request.Builder().url("http://localhost:8085/recipes")
				.addHeader("Authorization", token).post(recipeBody).build();
		Response postResponse = client.newCall(recipeRequest).execute();
		assertEquals(401, loginResponse.code(),
				() -> "login should return unauthorized status code.  Expected: 401, Actual: " + loginResponse.code());
		assertThat(token).isIn("", "Invalid credentials", "Invalid username or password");
		assertEquals(401, postResponse.code());

	}

	@Test
	public void testLogout() throws IOException {
		Chef chef = new Chef(1, "JoeCool", "snoopy@null.com", "redbarron", false);
		Recipe newRecipe = new Recipe(6, "fried fish", "fish, oil, stove", chef);
		RequestBody chefBody = RequestBody.create(
				"{\"username\":\"" + chef.getUsername() + "\", \"password\":\"" + chef.getPassword() + "\"}",
				MediaType.get("application/json; charset=utf-8"));
		Request loginRequest = new Request.Builder().url(BASE_URL + "/login").post(chefBody).build();
		Response loginResponse = client.newCall(loginRequest).execute();
		String token = loginResponse.body().string();
		Request logoutRequest = new Request.Builder().url(BASE_URL + "/logout").post(chefBody)
				.addHeader("Authorization", "Bearer " + token).build();
		Response logoutResponse = client.newCall(logoutRequest).execute();
		RequestBody recipeBody = RequestBody.create(new JavalinJackson().toJsonString(newRecipe, Recipe.class),
				MediaType.get("application/json; charset=utf-8"));

		Request recipeRequest = new Request.Builder().url("http://localhost:8085/recipes")
				.addHeader("Authorization", "Bearer " + token).post(recipeBody).build();
		Response postResponse = client.newCall(recipeRequest).execute();
		Request getRequest = new Request.Builder().url(BASE_URL + "/recipes/6").get().build();
		Response getResponse = client.newCall(getRequest).execute();
		assertEquals(200, loginResponse.code(),
				() -> "login should return a success status code.  Expected: 200, Actual: " + loginResponse.code());
		assertEquals(200, logoutResponse.code(), () -> "Logout should be successful");
		assertEquals(401, postResponse.code(), postResponse.body().string());
		assertEquals(404, getResponse.code(), () -> "recipe should not have been created");

	}

	@Test
	public void testRegister() throws IOException {

		Chef chef = new Chef(0, "new chef", "newchef@chefmail.com", "1234abc", false);
		RequestBody chefBody = RequestBody.create(
				"{\"username\": \"" + chef.getUsername() + "\", \"password\": \" " + chef.getPassword()
						+ "\", \"email\": \"" + chef.getEmail() + "\"}",
				MediaType.get("application/json; charset=utf-8"));
		Request registerRequest = new Request.Builder().url(BASE_URL + "/register").post(chefBody).build();
		Response registerResponse = client.newCall(registerRequest).execute();
		assertEquals(201, registerResponse.code(), () -> "Should successfully register user");
		RequestBody loginBody = RequestBody.create(
				"{\"username\": \"" + chef.getUsername() + "\", \"password\": \" " + chef.getPassword() + "\"}",
				MediaType.get("application/json; charset=utf-8"));
		Request loginRequest = new Request.Builder().url(BASE_URL + "/login").post(loginBody).build();
		Response loginResponse = client.newCall(loginRequest).execute();
		assertEquals(200, loginResponse.code(),
				() -> "login should return a success status code.  Expected: 200, Actual: " + loginResponse.code());
		assertNotNull(loginResponse.body().toString(), () -> "login should return a token in the body");

	}

}
