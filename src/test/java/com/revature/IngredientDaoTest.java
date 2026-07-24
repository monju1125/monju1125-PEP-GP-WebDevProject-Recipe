package com.revature;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.model.Ingredient;
import com.revature.dao.IngredientDAO;
import com.revature.util.ConnectionUtil;
import com.revature.util.DBUtil;
import com.revature.util.Page;
import com.revature.util.PageOptions;
import static com.revature.utils.TestingUtils.assertCountDifference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class IngredientDaoTest {
        private List<Ingredient> ingredientList = new ArrayList<>();
        private IngredientDAO ingredientDao;
        private String countSelStatement = "SELECT COUNT(*) FROM INGREDIENT";

        @BeforeEach
        void setupTestsData() throws SQLException {
                DBUtil.RUN_SQL(); // Create table
                ingredientList.clear();
                ingredientDao = new IngredientDAO(new ConnectionUtil());
                ingredientList.addAll(Arrays.asList(
                                new Ingredient(1, "carrot"),
                                new Ingredient(2, "potato"),
                                new Ingredient(3, "tomato"),
                                new Ingredient(4, "lemon"),
                                new Ingredient(5, "rice"),
                                new Ingredient(6, "stone")));
        }

        @Test
        public void createIngredientTest() {
                Ingredient ingredient = new Ingredient("testIngredient");
                assertCountDifference(1, "Expected Ingredient count to be 1 more", countSelStatement, () -> {
                        int newId = ingredientDao.createIngredient(ingredient);
                        // Verify that the created ingredient has a valid ID
                        assertEquals(7, newId, "The ID of the newly created ingredient should be 7");
                });
        }

        @Test
        public void readOneTest() {
                Ingredient ingredient = ingredientDao.getIngredientById(1);
                assertEquals(ingredientList.get(0), ingredient,
                                () -> "The returned ingredient doesn't match the expected ingredient. Expected: "
                                                + ingredientList.get(0) + " Actual: " + ingredient);
        }

        @Test
        public void deleteIngredientTest() throws SQLException {
                Ingredient ingredient = ingredientDao.getIngredientById(1);
                assertCountDifference(-1, "Expected Ingredient count to be 1 less", countSelStatement, () -> {
                        ingredientDao.deleteIngredient(ingredient);

                });

                // Ensure that the ingredient no longer exists in the database
                assertEquals(null, ingredientDao.getIngredientById(1),
                                "The ingredient with ID 1 should have been deleted.");
        }

        @Test
        public void updateIngredientTest() {
                Ingredient ingredient = ingredientDao.getIngredientById(1);
                ingredient.setName("newName");
                ingredientDao.updateIngredient(ingredient);
                Ingredient updatedIngredient = ingredientDao.getIngredientById(1);
                assertEquals(ingredient.getName(), updatedIngredient.getName(),
                                () -> "The returned ingredient name doesn't match the expected name. Expected: "
                                                + ingredient.getName()
                                                + " Actual: " + updatedIngredient.getName());
        }

        @Test
        public void getAllIngredientsTest() {
                List<Ingredient> ingredients = ingredientDao.getAllIngredients();
                assertEquals(ingredientList.size(), ingredients.size(),
                                "The number of returned ingredients should match the expected size.");
                assertIterableEquals(ingredientList, ingredients,
                                () -> "The returned ingredients don't match the expected ingredients. Expected: "
                                                + ingredientList
                                                + " Actual: " + ingredients);
        }

        @Test
        public void getAndPageAllIngredientsTest() {
                // Specify sortBy and sortDirection as "ID" and "ASC" for a consistent ordering
                PageOptions pageOptions = new PageOptions(1, 2, "ID", "ASC");

                // Adjust expectedIngredients to ensure it matches the sorted order
                Page<Ingredient> expectedIngredients = new Page<>(1, 2, ingredientList.size() / 2,
                                ingredientList.size(),
                                ingredientList.subList(0, 2));

                // Perform the query with pagination and sorting
                Page<Ingredient> ingredients = ingredientDao.getAllIngredients(pageOptions);

                // Verify that the items returned match the expected page of ingredients
                assertIterableEquals(expectedIngredients.getItems(), ingredients.getItems(),
                                "The returned ingredients don't match the expected ingredients.");
        }

        @Test
        public void searchIngredientsTest() {
                List<Ingredient> ingredients = ingredientDao.searchIngredients("to");
                List<Ingredient> expectedIngredients = Arrays.asList(ingredientList.get(1), ingredientList.get(2),
                                ingredientList.get(5));
                assertIterableEquals(expectedIngredients, ingredients,
                                () -> "The returned ingredients don't match the expected ingredients. Expected: "
                                                + expectedIngredients
                                                + " Actual: " + ingredients);
        }

        @Test
        public void searchAndPageIngredientsTest() {
                // Use a search term and specify page and sort options
                PageOptions pageOptions = new PageOptions(1, 2, "ID", "ASC");

                // Expected items are prepared to match the expected sort and page options
                Page<Ingredient> expectedIngredients = new Page<>(1, 2, 2, 3,
                                Arrays.asList(ingredientList.get(1), ingredientList.get(2)));

                // Perform the search with pagination and sorting
                Page<Ingredient> ingredients = ingredientDao.searchIngredients("to", pageOptions);

                // Verify that the search and page results match expectations
                assertEquals(expectedIngredients, ingredients,
                                "The returned ingredients don't match the expected ingredients.");
        }

}
