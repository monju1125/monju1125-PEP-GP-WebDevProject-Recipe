package com.revature;

import static org.mockito.Mockito.*;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.junit.jupiter.api.Test;

import com.revature.controller.RecipeController;
import com.revature.model.Recipe;
import com.revature.service.AuthenticationService;
import com.revature.service.RecipeService;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class RecipeControllerTest {

    @Test
    public void testGetRecipesWithRecipeName() throws Exception {
        RecipeService recipeService = mock(RecipeService.class);
        AuthenticationService authService = mock(AuthenticationService.class);
        List<Recipe> mockResults = Collections.singletonList(new Recipe("Grilled Cheese", "Grill bread and cheese"));
        when(recipeService.searchRecipes("Cheese")).thenReturn(mockResults);

        Context ctx = mock(Context.class);
        when(ctx.queryParam("name")).thenReturn("Cheese");

        Handler getRecipes = new RecipeController(recipeService, authService).fetchAllRecipes;
        getRecipes.handle(ctx);

        verify(ctx).status(200);
        verify(ctx).json(mockResults);
    }

    @Test
    public void testGetRecipesWithNoParams() throws Exception {
        RecipeService recipeService = mock(RecipeService.class);
        AuthenticationService authService = mock(AuthenticationService.class);
        List<Recipe> allRecipes = Arrays.asList(new Recipe("Apple Pie"), new Recipe("Grilled Cheese"),
                new Recipe("Steak"));
        when(recipeService.searchRecipes(null)).thenReturn(allRecipes);

        Context ctx = mock(Context.class);
        when(ctx.queryParam("name")).thenReturn(null);
        when(ctx.queryParam("ingredient")).thenReturn(null);

        Handler getRecipesHandler = new RecipeController(recipeService, authService).fetchAllRecipes;
        getRecipesHandler.handle(ctx);

        verify(ctx).status(200); // Set the response status code
        verify(ctx).json(allRecipes);
    }

    @Test
    public void testGetRecipesWithNoResults() throws Exception {
        RecipeService recipeService = mock(RecipeService.class);
        AuthenticationService authService = mock(AuthenticationService.class);
        when(recipeService.searchRecipes("Nonexistent Recipe")).thenReturn(Collections.emptyList());

        Context ctx = mock(Context.class);
        when(ctx.queryParam("name")).thenReturn("Nonexistent Recipe");

        Handler getRecipes = new RecipeController(recipeService, authService).fetchAllRecipes;
        getRecipes.handle(ctx);

        verify(ctx).status(404);
        verify(ctx).result("No recipes found");
    }
}
