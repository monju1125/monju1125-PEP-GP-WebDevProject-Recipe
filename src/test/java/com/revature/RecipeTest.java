package com.revature;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.revature.model.Recipe;
import com.revature.model.Chef;

public class RecipeTest {

	@Test
	public void testCreationOfRecipe() {
		Recipe Recipe = new Recipe();
		assertNotNull(Recipe, "Recipe created should not be null");
	}

	@Test
	public void testCreationOfRecipeWithName() {
		Recipe Recipe = new Recipe("carrot soup");
		assertNotNull(Recipe, "Recipe created should not be null");
	}

	@Test
	public void testSetRecipeName() {
		Recipe Recipe = new Recipe();
		Recipe.setName("carrot soup");
	}

	@Test
	public void testGetRecipeName() {
		Recipe Recipe = new Recipe("carrot soup");
		assertEquals("carrot soup", Recipe.getName(), ".getName should return name carrot");
	}

	@Test
	public void testSetRecipeInstructions() {
		Recipe recipe = new Recipe();
		recipe.setInstructions("Put carrot in water.  Boil.  Maybe salt.");
	}

	@Test
	public void testGetRecipeInstructions() {
		Recipe Recipe = new Recipe("carrot soup", "Put carrot in water.  Boil.  Maybe salt.");
		assertEquals("Put carrot in water.  Boil.  Maybe salt.", Recipe.getInstructions(),
				".setInstructions should return given instructions");
	}

	@Test
	public void testSetRecipeAuthor() {
		Recipe recipe = new Recipe();
		Chef author = new Chef();
		recipe.setAuthor(author);
	}

	@Test
	public void testGetRecipeAuthor() {
		Chef author = new Chef();
		Recipe Recipe = new Recipe(1, "carrot soup", "Put carrot in water.  Boil.  Maybe salt.", author);
		assertEquals(author, Recipe.getAuthor(), ".getUser should return given User");
	}

}
