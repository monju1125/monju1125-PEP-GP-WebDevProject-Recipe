package com.revature.model;

/**
 The RecipeIngredient class represents the relationships between ingredients and associated recipes. It stores the id and name of the relationship, as well as the volume and unit. This class provides getter and setter methods to access and modify the fields.

You do not need to edit this class.

 */
public class RecipeIngredient {
	
	// fields

	/** The unique identifier of the recipe-ingredient. */
	private int id;
	/** The name of the recipe-ingredient. */
	private String name;
	/** The amount needed for the recipe-ingredient. */
	private double volume;
	/** The measuring unit used for recipe-ingredient. */
	private String unit;

	// constructors
	public RecipeIngredient() {
		super();
	}

	public RecipeIngredient(Ingredient ingredient, double volume, String unit) {
		super();
		this.id = ingredient.getId();
		this.name = ingredient.getName();
		this.volume = volume;
		this.unit = unit;
	}

	public RecipeIngredient(int id, String name, double volume, String unit) {
		super();
		this.id = id;
		this.name = name;
		this.volume = volume;
		this.unit = unit;
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

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	
	
}
