package com.revature;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.revature.model.Chef;
import com.revature.model.Recipe;
import com.revature.dao.ChefDAO;
import com.revature.dao.RecipeDAO;
import com.revature.util.ConnectionUtil;
import com.revature.util.Page;
import com.revature.util.PageOptions;

public class RecipeDaoTest {

    @Mock
    private ConnectionUtil connectionUtil;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ChefDAO chefDao;

    @InjectMocks
    private RecipeDAO recipeDao = new RecipeDAO(chefDao, null, null);

    private List<Recipe> recipeList;
    private List<Chef> chefList;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        // Initialize test data
        chefList = Arrays.asList(
                new Chef(1, "JoeCool", "snoopy@null.com", "redbarron", false),
                new Chef(2, "CharlieBrown", "goodgrief@peanuts.com", "thegreatpumpkin", false));

        recipeList = Arrays.asList(
                new Recipe(1, "carrot soup", "Put carrot in water. Boil. Maybe salt.", chefList.get(0)),
                new Recipe(2, "potato soup", "Put potato in water. Boil. Maybe salt.", chefList.get(1)));

        // Set up mock behavior
        when(connectionUtil.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);

    }

    @Test
    public void getRecipeById_Success() throws SQLException {
        // Arrange
        Recipe expectedRecipe = recipeList.get(0);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(expectedRecipe.getId());
        when(resultSet.getString("name")).thenReturn(expectedRecipe.getName());
        when(resultSet.getString("instructions")).thenReturn(expectedRecipe.getInstructions());
        when(resultSet.getInt("chef_id")).thenReturn(expectedRecipe.getAuthor().getId());
        when(chefDao.getChefById(anyInt())).thenReturn(expectedRecipe.getAuthor());

        // Act
        Recipe actualRecipe = recipeDao.getRecipeById(1);

        // Assert
        assertEquals(expectedRecipe, actualRecipe);

        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    public void getAllRecipes_Success() throws SQLException {
        // Arrange
        String expectedSQL = "SELECT * FROM RECIPE ORDER BY id";
        when(connectionUtil.getConnection()).thenReturn(connection); // Mock the connection
        when(connection.createStatement()).thenReturn(preparedStatement); // Mock the statement
        when(preparedStatement.executeQuery(expectedSQL)).thenReturn(resultSet); // Mock the query execution

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("carrot soup", "potato soup");
        when(resultSet.getString("instructions"))
                .thenReturn("Put carrot in water. Boil. Maybe salt.",
                        "Put potato in water. Boil. Maybe salt.");
        when(resultSet.getInt("chef_id")).thenReturn(1, 2);

        when(chefDao.getChefById(1)).thenReturn(chefList.get(0));
        when(chefDao.getChefById(2)).thenReturn(chefList.get(1));

        // Act
        List<Recipe> actualRecipes = recipeDao.getAllRecipes();

        // Assert
        assertEquals(recipeList, actualRecipes);
        verify(connection).createStatement(); // Verify the statement creation
        verify(preparedStatement).executeQuery(expectedSQL); // Verify the query execution
        verify(resultSet, times(3)).next(); // Verify result set navigation
    }

    @Test
    public void createRecipe_Success() throws SQLException {
        // Arrange
        Recipe recipeToCreate = new Recipe(0, "test recipe", "test instructions", chefList.get(0));
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(3);

        // Act
        int newId = recipeDao.createRecipe(recipeToCreate);

        // Assert
        assertEquals(3, newId);
        verify(preparedStatement).setString(1, recipeToCreate.getName());
        verify(preparedStatement).setString(2, recipeToCreate.getInstructions());
        verify(preparedStatement).setInt(3, recipeToCreate.getAuthor().getId());
    }

    @Test
    public void updateRecipe_Success() throws SQLException {
        // Arrange
        Recipe recipeToUpdate = recipeList.get(0);
        recipeToUpdate.setName("updated name");
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        recipeDao.updateRecipe(recipeToUpdate);

        // Assert
        verify(preparedStatement).setString(1, recipeToUpdate.getInstructions());
        verify(preparedStatement).setInt(2, recipeToUpdate.getAuthor().getId());
        verify(preparedStatement).setInt(3, recipeToUpdate.getId());
    }

    @Test
    public void deleteRecipe_Success() throws SQLException {
        // Arrange
        Recipe recipeToDelete = recipeList.get(0);
        when(preparedStatement.executeUpdate()).thenReturn(1); // Mock the execution result

        // Act
        recipeDao.deleteRecipe(recipeToDelete);

        // Assert
        verify(preparedStatement, times(2)).setInt(1, recipeToDelete.getId()); // Verify setInt was called twice
        verify(preparedStatement, times(2)).executeUpdate(); // Verify executeUpdate was called twice
    }

    @Test
    public void searchRecipesByTerm_Success() throws SQLException {
        // Arrange
        String searchTerm = "soup";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("carrot soup", "potato soup");
        when(resultSet.getString("instructions"))
                .thenReturn("Put carrot in water. Boil. Maybe salt.",
                        "Put potato in water. Boil. Maybe salt.");
        when(resultSet.getInt("author_id")).thenReturn(1, 2);
        when(chefDao.getChefById(1)).thenReturn(chefList.get(0));
        when(chefDao.getChefById(2)).thenReturn(chefList.get(1));

        // Act
        List<Recipe> results = recipeDao.searchRecipesByTerm(searchTerm);

        // Assert
        assertEquals(recipeList, results);
        verify(preparedStatement).setString(1, "%" + searchTerm + "%");
    }

    @Test
    public void getAllRecipesPaged_Success() throws SQLException {
        // Arrange
        PageOptions pageable = new PageOptions(1, 2);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("carrot soup", "potato soup");
        when(resultSet.getString("instructions"))
                .thenReturn("Put carrot in water. Boil. Maybe salt.",
                        "Put potato in water. Boil. Maybe salt.");
        when(resultSet.getInt("chef_id")).thenReturn(1, 2);
        when(chefDao.getChefById(1)).thenReturn(chefList.get(0));
        when(chefDao.getChefById(2)).thenReturn(chefList.get(1));

        // Act
        Page<Recipe> recipePage = recipeDao.getAllRecipes(pageable);

        // Assert
        assertEquals(2, recipePage.getItems().size());
        assertEquals(2, recipePage.getPageSize());
    }
}
