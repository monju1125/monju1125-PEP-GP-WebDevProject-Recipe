package com.revature.model;

/**
The Ingredient class represents an ingredient used in recipes. It stores basic information about the ingredient, such as its unique id and name. This class provides getter and setter methods to access and modify the fields and overrides methods Object class methods.

You do not need to edit this class.

 */
public class Ingredient {

    // fields
    
    /** The unique identifier of the ingredient. */
    private int id;
    /** The name of the ingredient. */
    private String name;

    // constructors
    public Ingredient() {
    }

    public Ingredient(String name) {
        this.name = name;
    }

    public Ingredient(int id, String name) {
        this.id = id;
        this.name = name;
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

    /**
     * Compares this Ingredient object with another object for equality.
     * 
     * @param other the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true; // Check for reference equality
        if (other == null || getClass() != other.getClass()) return false; // Check for null and class match
        Ingredient ingredient = (Ingredient) other; // Typecast to Ingredient
        return id == ingredient.id && // Compare IDs
               name.equals(ingredient.name); // Compare names
    }

    /**
     * Generates the hash code for this Ingredient object.
     * 
     * @return the hash code of the ingredient
     */
    @Override
    public int hashCode() {
        int result = 17; 
        result = 31 * result + id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    /**
     * Returns a string representation of the Ingredient object.
     * This includes the object's `id` and `name` in a structured format.
     * 
     * @return string representation of the Ingredient object, including the `id` and `name`.
     */
    @Override
    public String toString() {
        return "Ingredient{" +
               "id=" + id +
               ", name='" + name + '\'' +
               '}';
    }
}
