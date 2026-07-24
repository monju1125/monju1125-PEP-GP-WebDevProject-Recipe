package com.revature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.revature.model.Ingredient;
import com.revature.model.Recipe;
import com.revature.dao.IngredientDAO;
import com.revature.service.IngredientService;
import com.revature.util.Page;
import com.revature.util.PageOptions;

public class IngredientServiceTest {
    private IngredientService ingredientService;
    private IngredientDAO ingredientDao;
    List<Ingredient> MOCKS;

    @BeforeEach
    void setUpMocks() {
        ingredientDao = mock(IngredientDAO.class);
        ingredientService = new IngredientService(ingredientDao);
        MOCKS = Arrays.asList(
                new Ingredient(1, "carrot"),
                new Ingredient(2, "potato"),
                new Ingredient(3, "tomato"),
                new Ingredient(4, "lemon"),
                new Ingredient(5, "rice"),
                new Ingredient(6, "stone"));
    }

    @Test
    public void fetchOneIngredient() {
        when(ingredientDao.getIngredientById(1)).thenReturn(MOCKS.get(0));
        Optional<Ingredient> ingredient = ingredientService.findIngredient(1);
        assertTrue(ingredient.isPresent(), () -> "Ingredient should be present");
        assertEquals(MOCKS.get(0), ingredient.get(), () -> "Ingredient should match");
    }

    @Test
    public void failToFetchOneIngredient() {
        when(ingredientDao.getIngredientById(1)).thenReturn(null);
        Optional<Ingredient> recipe = ingredientService.findIngredient(1);
        assertTrue(recipe.isEmpty(), () -> "Ingredient should not be present");
    }

    @Test
    public void saveNewIngredient() {
        Ingredient newIngredient = new Ingredient("new ingredient");
        ArgumentCaptor<Ingredient> ingredientCaptor = ArgumentCaptor.forClass(Ingredient.class);
        when(ingredientDao.createIngredient(any(Ingredient.class))).thenReturn(42);
        ingredientService.saveIngredient(newIngredient);
        verify(ingredientDao).createIngredient(ingredientCaptor.capture());
        Ingredient captureIngredient = ingredientCaptor.getValue();
        assertEquals(42, captureIngredient.getId(), () -> "Services should set the id of newly created ingredient");
    }

    @Test
    public void updateIngredient() {
        Ingredient existingIngredient = new Ingredient(42, "new ingredient");
        ArgumentCaptor<Ingredient> ingredientCaptor = ArgumentCaptor.forClass(Ingredient.class);
        doNothing().when(ingredientDao).updateIngredient(any(Ingredient.class));
        ingredientService.saveIngredient(existingIngredient);
        verify(ingredientDao).updateIngredient(ingredientCaptor.capture());
        Ingredient captureIngredient = ingredientCaptor.getValue();
        assertEquals(42, captureIngredient.getId(), () -> "Services should not change the id of updated ingredients");
    }

    @Test
    public void delteIngredient() throws SQLException {
        when(ingredientDao.getIngredientById(1)).thenReturn(MOCKS.get(0));
        doNothing().when(ingredientDao).deleteIngredient(any(Ingredient.class));
        ArgumentCaptor<Ingredient> ingredientCaptor = ArgumentCaptor.forClass(Ingredient.class);
        ingredientService.deleteIngredient(1);
        verify(ingredientDao).deleteIngredient(ingredientCaptor.capture());
        verify(ingredientDao).getIngredientById(1);
    }

    @Test
    public void searchForListOfAllIngredient() {
        when(ingredientDao.getAllIngredients()).thenReturn(MOCKS);
        List<Ingredient> ingredients = ingredientService.searchIngredients(null);
        assertIterableEquals(MOCKS, ingredients, () -> "Ingredients should match");
    }

    @Test
    public void searchForFilteredListOfIngredients() {
        when(ingredientDao.searchIngredients("to")).thenReturn(Arrays.asList(MOCKS.get(1), MOCKS.get(2), MOCKS.get(5)));
        List<Ingredient> ingredients = ingredientService.searchIngredients("to");
        assertIterableEquals(Arrays.asList(MOCKS.get(1), MOCKS.get(2), MOCKS.get(5)), ingredients,
                () -> "Ingredients should match");
    }

    @Test
    public void searchReturnsEmptyList() {
        when(ingredientDao.searchIngredients("Bal")).thenReturn(Collections.emptyList());
        List<Ingredient> ingredients = ingredientService.searchIngredients("Bal");
        assertTrue(ingredients.isEmpty(), () -> "Ingredients should be empty");
    }

    @Test
    public void searchForPageOfAllRecipes() {
        when(ingredientDao.getAllIngredients(any(PageOptions.class)))
                .thenReturn(new Page<Ingredient>(1, 5, 1, 5, MOCKS));
        Page<Ingredient> ingredients = ingredientService.searchIngredients(null, 1, 5, "id", "asc");
        ArgumentCaptor<PageOptions> optionsCaptor = ArgumentCaptor.forClass(PageOptions.class);
        verify(ingredientDao).getAllIngredients(optionsCaptor.capture());
        assertEquals(new Page<Ingredient>(1, 5, 1, 5, MOCKS), ingredients,
                () -> "Service shouldn't change the page returned from the dao");
    }

    @Test
    public void serchForFilteredPageOfRecipes() {
        when(ingredientDao.searchIngredients(anyString(), any(PageOptions.class)))
                .thenReturn(new Page<Ingredient>(1, 3, 1, 3, Arrays.asList(MOCKS.get(1), MOCKS.get(2), MOCKS.get(5))));
        Page<Ingredient> ingredients = ingredientService.searchIngredients("to", 1, 5, "id", "asc");
        ArgumentCaptor<PageOptions> optionsCaptor = ArgumentCaptor.forClass(PageOptions.class);
        ArgumentCaptor<String> termCaptor = ArgumentCaptor.forClass(String.class);
        verify(ingredientDao).searchIngredients(termCaptor.capture(), optionsCaptor.capture());
        assertEquals(new Page<Ingredient>(1, 3, 1, 3, Arrays.asList(MOCKS.get(1), MOCKS.get(2), MOCKS.get(5))),
                ingredients, () -> "Service shouldn't change the page returned from the dao");
    }

    @Test
    public void searchReturnsEmptyPage() {
        when(ingredientDao.searchIngredients(anyString(), any(PageOptions.class)))
                .thenReturn(new Page<Ingredient>(0, 0, 0, 0, Collections.emptyList()));
        Page<Ingredient> ingredients = ingredientService.searchIngredients("Bal", 1, 5, "id", "asc");
        ArgumentCaptor<PageOptions> optionsCaptor = ArgumentCaptor.forClass(PageOptions.class);
        ArgumentCaptor<String> termCaptor = ArgumentCaptor.forClass(String.class);
        verify(ingredientDao).searchIngredients(termCaptor.capture(), optionsCaptor.capture());
        assertEquals(new Page<Recipe>(0, 0, 0, 0, Collections.emptyList()), ingredients,
                () -> "Service shouldn't change the page returned from the dao");
    }
}
