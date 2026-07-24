package com.revature.model;

import java.util.List;

/**
 The Recipe class represents the domain object for a recipe. It stores the chef's basic information such as id, name, instructions, authors, and associated ingredients. This class provides getter and setter methods to access and modify the fields and overrides methods Object class methods.
*/
public class Recipe {

    // fields
    private int id;
    private String name;
    private String instructions;
    private Chef author;
	private List<RecipeIngredient> ingredients;

    // constructors
    public Recipe() {
        // No-arg constructor
    }

    public Recipe(String name) {
        this.name = name;
    }

    public Recipe(String name, String instructions) {
        this.name = name;
        this.instructions = instructions;
    }

    public Recipe(int id, String name, String instructions, Chef author) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.author = author;
    }

    // getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Chef getAuthor() {
        return author;
    }

    public void setAuthor(Chef author) {
        this.author = author;
    }

    public List<RecipeIngredient> getIngredients() {
		return ingredients;
	}

	public void setIngredients(List<RecipeIngredient> ingredients) {
		this.ingredients = ingredients;
	}

    /**
     * Generates the hash code for this Recipe object.
     *
     * @return the hash code of the recipe
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id; 
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    /**
     * Compares this Recipe object with another object for equality.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Check for reference equality
        if (obj == null || getClass() != obj.getClass()) return false; // Check for null and class match
        Recipe recipe = (Recipe) obj; // Typecast to Recipe
        return id == recipe.id && // Compare IDs
               name.equals(recipe.name); // Compare names
    }

    /**
     * Returns a string representation of the Ingredient object.
     * This includes the object's `id`, `name`, `instructions`, and `author` in a structured format.
     * 
     * @return string representation of the Ingredient object, including the `id`, `name`, `instructions`, and `author`.
     */
    @Override
    public String toString() {
        return "Recipe{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", instructions='" + instructions + '\'' +
               ", author=" + (author != null ? author.getUsername() : "Unknown") +
               '}';
    }
}
