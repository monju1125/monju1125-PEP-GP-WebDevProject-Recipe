package com.revature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.model.Chef;
import com.revature.dao.ChefDAO;
import com.revature.util.ConnectionUtil;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ChefDaoTest {

    @Mock
    private ConnectionUtil connectionUtil;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private ChefDAO chefDAO = new ChefDAO(new ConnectionUtil());

    private Chef testChef;

    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() throws Exception {
        openMocks = MockitoAnnotations.openMocks(this);

        // Setup common mock behaviors
        when(connectionUtil.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(statement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        // Create test chef data
        testChef = new Chef(1, "testChef", "test@chef.com", "password123", false);
    }

    @AfterEach
    public void tearDownTests() throws Exception {
        openMocks.close();
    }

    @Test
    public void testGetAllChefs() throws Exception {
        // Arrange
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("username")).thenReturn("chef1", "chef2");
        when(resultSet.getString("email")).thenReturn("chef1@test.com", "chef2@test.com");
        when(resultSet.getString("password")).thenReturn("pass1", "pass2");
        when(resultSet.getBoolean("is_admin")).thenReturn(false, false);

        // Act
        List<Chef> chefs = chefDAO.getAllChefs();

        // Assert
        assertNotNull(chefs);
        assertEquals(2, chefs.size());
        assertEquals("chef1", chefs.get(0).getUsername());
        assertEquals("chef2", chefs.get(1).getUsername());

        verify(statement).executeQuery("SELECT * FROM CHEF ORDER BY id");
    }

    @Test
    public void testGetChefById() throws Exception {
        // Arrange
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("username")).thenReturn("testChef");
        when(resultSet.getString("email")).thenReturn("test@chef.com");
        when(resultSet.getString("password")).thenReturn("password123");
        when(resultSet.getBoolean("is_admin")).thenReturn(false);

        // Act
        Chef chef = chefDAO.getChefById(1);

        // Assert
        assertNotNull(chef);
        assertEquals(1, chef.getId());
        assertEquals("testChef", chef.getUsername());
        assertEquals("test@chef.com", chef.getEmail());

        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    public void testCreateChef() throws Exception {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        // Act
        int newId = chefDAO.createChef(testChef);

        // Assert
        assertEquals(1, newId);
        verify(preparedStatement).setString(1, testChef.getUsername());
        verify(preparedStatement).setString(2, testChef.getEmail());
        verify(preparedStatement).setString(3, testChef.getPassword());
        verify(preparedStatement).setBoolean(4, testChef.isAdmin());
    }

    @Test
    public void testUpdateChef() throws Exception {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        chefDAO.updateChef(testChef);

        // Assert
        verify(preparedStatement).setString(1, testChef.getUsername());
        verify(preparedStatement).setString(2, testChef.getEmail());
        verify(preparedStatement).setString(3, testChef.getPassword());
        verify(preparedStatement).setBoolean(4, testChef.isAdmin());
        verify(preparedStatement).setInt(5, testChef.getId());
    }

    @Test
    public void testDeleteChef() throws Exception {
        // Arrange
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        chefDAO.deleteChef(testChef);

        // Assert
        verify(preparedStatement).setInt(1, testChef.getId());
        verify(preparedStatement).executeUpdate();
    }
}
