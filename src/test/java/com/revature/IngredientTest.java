package com.revature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.revature.model.Ingredient;

public class IngredientTest {
    Class<?> ingredientClass = Ingredient.class;

    @Test
    public void noArgsConstructor() {
        try {
            ingredientClass.getConstructor();
        } catch (NoSuchMethodException ex) {
            fail("The Ingredient class should have a no-args constructor", ex);
        }
    }

    @Test
    public void parameterizedConstructors() {
        try {
            ingredientClass.getConstructor(String.class);
            ingredientClass.getConstructor(int.class, String.class);

        } catch (NoSuchMethodException ex) {
            fail("The Ingredient class doesn't contain the correct parameterized constructors.", ex);
        }
    }

    @Test
    public void instanceVariables() {
        try {
            ingredientClass.getDeclaredField("id");
            ingredientClass.getDeclaredField("name");
        } catch (NoSuchFieldException ex) {
            fail("The Ingredient class should have an id and name instance variable.", ex);
        }
    }

    @Test
    public void getterMethods() {
        try {
            ingredientClass.getMethod("getId");
            ingredientClass.getMethod("getName");
        } catch (NoSuchMethodException ex) {
            fail("The Ingredient class should have getter methods for id and name.", ex);
        }

    }

    @Test
    public void setterMethods() {
        try {
            ingredientClass.getMethod("setId", int.class);
            ingredientClass.getMethod("setName", String.class);
        } catch (NoSuchMethodException ex) {
            fail("The Ingredient class should have setter methods for id and name.", ex);
        }
    }

    @Test
    public void objectContract() {
        try {
            ingredientClass.getDeclaredMethod("equals", Object.class);
            ingredientClass.getDeclaredMethod("hashCode");
        } catch (NoSuchMethodException ex) {
            fail("The Ingredient class should have overridden the equals and hashCode methods.", ex);
        }
    }

    @Test
    public void settersAndGettersImpl() {
        Ingredient i = new Ingredient();
        i.setId(1);
        i.setName("test");
        assertEquals(1, i.getId(), "The Ingredient class should have implemented the setId and getId methods.");
        assertEquals("test", i.getName(),
                "The Ingredient class should have implemented the setName and getName methods.");
    }
}
