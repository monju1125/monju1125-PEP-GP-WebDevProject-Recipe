package com.revature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.controller.IngredientController;
import com.revature.dao.IngredientDAO;
import com.revature.service.IngredientService;
import com.revature.util.ConnectionUtil;
import com.revature.util.DBUtil;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;

public class IngredientIntegrationTest {
    private Javalin app;
    private IngredientDAO ingredientDao;
    private IngredientService ingredientService;
    private IngredientController ingredientController;

    @BeforeEach
    void setUp() throws SQLException {
        DBUtil.RUN_SQL();
        app = Javalin.create();
        ingredientDao = new IngredientDAO(new ConnectionUtil());
        ingredientService = new IngredientService(ingredientDao);
        ingredientController = new IngredientController(ingredientService);
        ingredientController.configureRoutes(app);
    }

    @Test
    public void testGetIngredient() {
        JavalinTest.test(app, (server, client) -> {
            assertEquals(200, client.get("/ingredients/1").code());
            assertEquals("{\"id\":1,\"name\":\"carrot\"}", client.get("/ingredients/1").body().string());
        });
    }

    @Test
    public void testGetIngredientNotFound() {
        JavalinTest.test(app, (server, client) -> {
            assertEquals(404, client.get("/ingredients/100").code());
        });
    }

    @Test
    public void testDeleteIngredient() {
        JavalinTest.test(app, (server, client) -> {
            assertEquals(204, client.delete("/ingredients/1").code());
        });
    }

    @Test
    public void testUpdateIngredient() {
        JavalinTest.test(app, (server, client) -> {
            assertEquals(204, client.put("/ingredients/1", "{\"id\": 1, \"name\": \"parsnips\"}").code());
            assertEquals("{\"id\":1,\"name\":\"parsnips\"}", client.get("/ingredients/1").body().string());
        });
    }

    @Test
    public void testUpdateNotFound() {
        JavalinTest.test(app, (server, client) -> {
            assertEquals(404, client.put("/ingredients/100", "{\"id\": 1, \"name\": \"parsnips\"}").code());
        });
    }

    @Test
    public void testCreateIngredient() {
        JavalinTest.test(app, (server, client) -> {
            assertEquals(201, client.post("/ingredients", "{\"name\": \"parsnips\"}").code());
            assertEquals("{\"id\":7,\"name\":\"parsnips\"}", client.get("/ingredients/7").body().string());
        });
    }

    @Test
    public void testGetIngredients() {
        JavalinTest.test(app, (server, client) -> {
            assertEquals(200, client.get("/ingredients").code());
            assertEquals(
                    "[{\"id\":1,\"name\":\"carrot\"},{\"id\":2,\"name\":\"potato\"},{\"id\":3,\"name\":\"tomato\"},{\"id\":4,\"name\":\"lemon\"},{\"id\":5,\"name\":\"rice\"},{\"id\":6,\"name\":\"stone\"}]",
                    client.get("/ingredients").body().string());
        });
    }

    @Test
    public void testPageIngredients() {
        JavalinTest.test(app, (server, client) -> {
            assertEquals(200, client.get("/ingredients?page=1&pageSize=2").code());
            assertEquals(
                    "{\"pageNumber\":1,\"pageSize\":2,\"totalPages\":3,\"totalElements\":6,\"items\":[{\"id\":1,\"name\":\"carrot\"},{\"id\":2,\"name\":\"potato\"}]}",
                    client.get("/ingredients?page=1&pageSize=2").body().string());
        });
    }

    @Test
    public void testGetIngredientsByTerm() {
        JavalinTest.test(app, (server, client) -> {
            assertEquals(200, client.get("/ingredients?term=to").code());
            assertEquals(
                    "[{\"id\":2,\"name\":\"potato\"},{\"id\":3,\"name\":\"tomato\"},{\"id\":6,\"name\":\"stone\"}]",
                    client.get("/ingredients?term=to").body().string());
        });
    }

    @Test
    public void testGetIngredientsByTermSorted() {
        JavalinTest.test(app, (server, client) -> {
            assertEquals(200,
                    client.get("/ingredients?term=to&sortBy=name&sortDirection=desc&page=1&pageSize=3").code());
            assertEquals(
                    "{\"pageNumber\":1,\"pageSize\":3,\"totalPages\":1,\"totalElements\":3,\"items\":[{\"id\":3,\"name\":\"tomato\"},{\"id\":6,\"name\":\"stone\"},{\"id\":2,\"name\":\"potato\"}]}",
                    client.get("/ingredients?term=to&sortBy=name&sortDirection=desc&page=1&pageSize=3").body()
                            .string());
        });
    }

}
