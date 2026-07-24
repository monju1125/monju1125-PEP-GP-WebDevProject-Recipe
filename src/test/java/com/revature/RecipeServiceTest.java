package com.revature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.revature.model.Recipe;
import com.revature.dao.RecipeDAO;
import com.revature.service.RecipeService;
import com.revature.util.Page;
import com.revature.util.PageOptions;

public class RecipeServiceTest {
    private RecipeService recipeService;
    private RecipeDAO recipeDao;
    private List<Recipe> MOCKS;

    @BeforeEach
    void setUpMocks() {
        recipeDao = mock(RecipeDAO.class);
        recipeService = new RecipeService(recipeDao);
        MOCKS = Arrays.asList(
                new Recipe(1, "Pasta", "Boil water, add pasta, add sauce", null),
                new Recipe(2, "Pizza", "Make dough, add sauce, add cheese, bake", null),
                new Recipe(3, "Salad", "Chop lettuce, add dressing", null),
                new Recipe(4, "Sandwich", "Put stuff between bread", null),
                new Recipe(5, "Soup", "Boil water, add stuff", null));
    }

    @Test
    public void fetchOneRecipe() {
        when(recipeDao.getRecipeById(1)).thenReturn(MOCKS.get(0));
        Optional<Recipe> recipe = recipeService.findRecipe(1);
        assertTrue(recipe.isPresent(), () -> "Recipe should be present");
        assertEquals(MOCKS.get(0), recipe.get(), () -> "Recipe should match");
    }

    @Test
    public void failToFetchOneRecipe() {
        when(recipeDao.getRecipeById(1)).thenReturn(null);
        Optional<Recipe> recipe = recipeService.findRecipe(1);
        assertTrue(recipe.isEmpty(), () -> "Recipe should not be present");
    }

    @Test
    public void saveNewRecipe() {
        Recipe newRecipe = new Recipe("New Recipe", "New Recipe Instructions");
        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        when(recipeDao.createRecipe(any(Recipe.class))).thenReturn(42);
        recipeService.saveRecipe(newRecipe);
        verify(recipeDao).createRecipe(recipeCaptor.capture());
        Recipe captureRecipe = recipeCaptor.getValue();
        assertEquals(42, captureRecipe.getId(), () -> "Services should set the id of newly created recipes");
    }

    @Test
    public void updateRecipe() {
        Recipe existingRecipe = new Recipe(42, "Existing Recipe", "Existing Recipe Instructions", null);
        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        doNothing().when(recipeDao).updateRecipe(any(Recipe.class));
        when(recipeDao.getRecipeById(anyInt())).thenReturn(existingRecipe);
        recipeService.saveRecipe(existingRecipe);
        verify(recipeDao).updateRecipe(recipeCaptor.capture());
        Recipe captureRecipe = recipeCaptor.getValue();
        assertEquals(42, captureRecipe.getId(), () -> "Services should not change the id of existing recipes");
    }

    @Test
    public void deleteRecipe() {
        when(recipeDao.getRecipeById(1)).thenReturn(MOCKS.get(0));
        doNothing().when(recipeDao).deleteRecipe(any(Recipe.class));
        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        recipeService.deleteRecipe(1);
        verify(recipeDao).deleteRecipe(recipeCaptor.capture());
        verify(recipeDao).getRecipeById(1);
    }

    @Test
    public void searchForListOfAllRecipes() {
        when(recipeDao.getAllRecipes()).thenReturn(MOCKS);
        List<Recipe> recipes = recipeService.searchRecipes(null);
        assertIterableEquals(MOCKS, recipes, () -> "Recipes should match");
    }

    @Test
    public void searchForFilteredListOfRecipes() {
        when(recipeDao.searchRecipesByTerm("a"))
                .thenReturn(Arrays.asList(MOCKS.get(0), MOCKS.get(2), MOCKS.get(3), MOCKS.get(4)));
        List<Recipe> recipes = recipeService.searchRecipes("a");
        assertIterableEquals(Arrays.asList(MOCKS.get(0), MOCKS.get(2), MOCKS.get(3), MOCKS.get(4)), recipes,
                () -> "Recipes should match");
    }

    @Test
    public void searchReturnsEmptyList() {
        when(recipeDao.searchRecipesByTerm("Bal")).thenReturn(Collections.emptyList());
        List<Recipe> recipes = recipeService.searchRecipes("Bal");
        assertTrue(recipes.isEmpty(), () -> "Recipes should be empty");
    }

    @Test
    public void searchForPageOfAllRecipes() {
        when(recipeDao.getAllRecipes(any(PageOptions.class))).thenReturn(new Page<Recipe>(1, 5, 1, 5, MOCKS));
        Page<Recipe> recipes = recipeService.searchRecipes(null, 1, 5, "id", "asc");
        ArgumentCaptor<PageOptions> optionsCaptor = ArgumentCaptor.forClass(PageOptions.class);
        verify(recipeDao).getAllRecipes(optionsCaptor.capture());
        assertEquals(new Page<Recipe>(1, 5, 1, 5, MOCKS), recipes,
                () -> "Service shouldn't change the page returned from the dao");
    }

    @Test
    public void serchForFilteredPageOfRecipes() {
        when(recipeDao.searchRecipesByTerm(anyString(), any(PageOptions.class))).thenReturn(
                new Page<Recipe>(1, 5, 1, 5, Arrays.asList(MOCKS.get(0), MOCKS.get(2), MOCKS.get(3), MOCKS.get(4))));
        Page<Recipe> recipes = recipeService.searchRecipes("a", 1, 5, "id", "asc");
        ArgumentCaptor<PageOptions> optionsCaptor = ArgumentCaptor.forClass(PageOptions.class);
        ArgumentCaptor<String> termCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipeDao).searchRecipesByTerm(termCaptor.capture(), optionsCaptor.capture());
        assertEquals(
                new Page<Recipe>(1, 5, 1, 5, Arrays.asList(MOCKS.get(0), MOCKS.get(2), MOCKS.get(3), MOCKS.get(4))),
                recipes, () -> "Service shouldn't change the page returned from the dao");
    }

    @Test
    public void searchReturnsEmptyPage() {
        when(recipeDao.searchRecipesByTerm(anyString(), any(PageOptions.class)))
                .thenReturn(new Page<Recipe>(1, 5, 0, 0, Collections.emptyList()));
        Page<Recipe> recipes = recipeService.searchRecipes("Bal", 1, 5, "id", "asc");
        ArgumentCaptor<PageOptions> optionsCaptor = ArgumentCaptor.forClass(PageOptions.class);
        ArgumentCaptor<String> termCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipeDao).searchRecipesByTerm(termCaptor.capture(), optionsCaptor.capture());
        assertEquals(new Page<Recipe>(1, 5, 0, 0, Collections.emptyList()), recipes,
                () -> "Service shouldn't change the page returned from the dao");
    }
}
