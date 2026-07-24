package com.revature;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.javalin.Javalin;

public class RecipePersistenceTest {

    private static Javalin server;
    private static final int PORT = 8083;

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static Javalin app;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RecipePersistenceTest.class.getName());
    @SuppressWarnings("unused")
    private static Process httpServerProcess;
    @SuppressWarnings("unused")
    private static String browserType;

    // Architecture and system detection
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String OS_ARCH = System.getProperty("os.arch").toLowerCase();
    private static final boolean IS_ARM = OS_ARCH.contains("aarch64") || OS_ARCH.contains("arm");
    private static final boolean IS_WINDOWS = OS_NAME.contains("windows");
    @SuppressWarnings("unused")
    private static final boolean IS_LINUX = OS_NAME.contains("linux");
    private static final boolean IS_MAC = OS_NAME.contains("mac");

    @BeforeAll
    public static void setUp() throws InterruptedException {
        try {
            printEnvironmentInfo();

            // Start the backend programmatically
            int port = 8081;
            app = Main.startServer(port, true);

            // Starting the static Javalin Server

            System.out.println("Starting local static web server for frontend files...");
            server = Javalin.create(config -> {
                config.staticFiles.add("/public/frontend");
            }).start(PORT);
            System.out.println("Static server running at: http://localhost:" + PORT);

            // Detect browser and driver
            BrowserConfig browserConfig = detectBrowserAndDriver();
            browserType = browserConfig.browserType;

            // Create WebDriver with appropriate configuration
            driver = createWebDriver(browserConfig);

            // Initialize WebDriverWait
            wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Set timeouts
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            System.out.println("WebDriver created successfully");

            Thread.sleep(1000);

            performLogin();

        } catch (Exception e) {
            System.err.println("\n=== SETUP FAILED ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();

            cleanup();
            throw new RuntimeException("Setup failed", e);
        }
    }

    @AfterAll
    public static void tearDown() {
        performLogout();

        System.out.println("\n=== TEARDOWN ===");
        cleanup();
        System.out.println("Teardown completed");
    }

    private void handleUnexpectedAlerts(WebDriver driver) {
        try {
            WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Unexpected Alert Text: " + alert.getText());
            alert.dismiss();
        } catch (TimeoutException e) {
            System.out.println("No unexpected alerts.");
        }
    }

    private void handleUnexpectedAlerts() {
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Alert detected: " + alert.getText());
            alert.dismiss();
        } catch (TimeoutException e) {
            System.out.println("No unexpected alerts.");
        }
    }

    private static void performLogin() {
        // go to relevant HTML page
        driver.get("http://localhost:8083/login/login-page.html");

        // perform login functionality
        WebElement usernameInput = driver.findElement(By.id("login-input"));
        WebElement passwordInput = driver.findElement(By.id("password-input"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        usernameInput.sendKeys("ChefTrevin");
        passwordInput.sendKeys("trevature");
        loginButton.click();

        // ensure we navigate to appropriate webpage
        wait.until(ExpectedConditions.urlContains("recipe-page")); // Wait for navigation to the recipe page

    }

    private static void performLogout() {
        // perform logout functionality
        WebElement logoutButton = driver.findElement(By.id("logout-button"));
        logoutButton.click();
    }

    private static void printEnvironmentInfo() {
        System.out.println("=== ENVIRONMENT INFO ===");
        System.out.println("OS: " + OS_NAME + " (" + OS_ARCH + ")");
        System.out.println("Architecture: " + (IS_ARM ? "ARM64" : "x86/x64"));
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Working directory: " + System.getProperty("user.dir"));
    }

    private static BrowserConfig detectBrowserAndDriver() {
        System.out.println("\n=== BROWSER AND DRIVER DETECTION ===");

        // First check for driver in project's "driver" folder
        BrowserConfig projectDriverConfig = checkProjectDriverFolder();
        if (projectDriverConfig != null) {
            return projectDriverConfig;
        }

        // Then check system-installed drivers
        BrowserConfig systemDriverConfig = checkSystemDrivers();
        if (systemDriverConfig != null) {
            return systemDriverConfig;
        }

        throw new RuntimeException("No compatible browser driver found");
    }

    private static BrowserConfig checkProjectDriverFolder() {
        File driverFolder = new File("driver");
        if (!driverFolder.exists() || !driverFolder.isDirectory()) {
            System.out.println("No 'driver' folder found in project root");
            return null;
        }

        System.out.println("Found 'driver' folder, checking for executables...");

        // Check for Edge driver first (since you mentioned x86 machines will have edge
        // driver)
        String[] edgeDriverNames = IS_WINDOWS ? new String[] { "msedgedriver.exe", "edgedriver.exe" }
                : new String[] { "msedgedriver", "edgedriver" };

        for (String driverName : edgeDriverNames) {
            File driverFile = new File(driverFolder, driverName);
            if (driverFile.exists()) {
                makeExecutable(driverFile);
                if (driverFile.canExecute()) {
                    System.out.println("Found Edge driver: " + driverFile.getAbsolutePath());
                    return new BrowserConfig("edge", driverFile.getAbsolutePath(), findEdgeBinary());
                }
            }
        }

        // Check for Chrome driver
        String[] chromeDriverNames = IS_WINDOWS ? new String[] { "chromedriver.exe" } : new String[] { "chromedriver" };

        for (String driverName : chromeDriverNames) {
            File driverFile = new File(driverFolder, driverName);
            if (driverFile.exists()) {
                makeExecutable(driverFile);
                if (driverFile.canExecute()) {
                    System.out.println("Found Chrome driver: " + driverFile.getAbsolutePath());
                    return new BrowserConfig("chrome", driverFile.getAbsolutePath(), findChromeBinary());
                }
            }
        }

        System.out.println("No compatible drivers found in 'driver' folder");
        return null;
    }

    private static BrowserConfig checkSystemDrivers() {
        System.out.println("Checking system-installed drivers...");

        // Chrome driver paths (prioritized for ARM systems)
        String[] chromeDriverPaths = {
                "/usr/bin/chromedriver",
                "/usr/local/bin/chromedriver",
                "/snap/bin/chromedriver",
                System.getProperty("user.home") + "/.cache/selenium/chromedriver/linux64/chromedriver",
                "/opt/chromedriver/chromedriver"
        };

        if (IS_WINDOWS) {
            chromeDriverPaths = new String[] {
                    "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe",
                    "C:\\ChromeDriver\\chromedriver.exe",
                    "chromedriver.exe" // In PATH
            };
        }

        for (String driverPath : chromeDriverPaths) {
            File driverFile = new File(driverPath);
            if (driverFile.exists() && driverFile.canExecute()) {
                System.out.println("Found system Chrome driver: " + driverPath);
                return new BrowserConfig("chrome", driverPath, findChromeBinary());
            }
        }

        // Edge driver paths
        if (IS_WINDOWS) {
            String[] edgeDriverPaths = {
                    "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedgedriver.exe",
                    "msedgedriver.exe" // In PATH
            };

            for (String driverPath : edgeDriverPaths) {
                File driverFile = new File(driverPath);
                if (driverFile.exists() && driverFile.canExecute()) {
                    System.out.println("Found system Edge driver: " + driverPath);
                    return new BrowserConfig("edge", driverPath, findEdgeBinary());
                }
            }
        }

        return null;
    }

    private static String findChromeBinary() {
        String[] chromePaths;

        if (IS_WINDOWS) {
            chromePaths = new String[] {
                    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
            };
        } else if (IS_MAC) {
            chromePaths = new String[] {
                    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
            };
        } else {
            chromePaths = new String[] {
                    "/usr/bin/chromium-browser",
                    "/usr/bin/chromium",
                    "/usr/bin/google-chrome",
                    "/snap/bin/chromium"
            };
        }

        for (String path : chromePaths) {
            if (new File(path).exists()) {
                System.out.println("Found Chrome binary: " + path);
                return path;
            }
        }

        System.out.println("Chrome binary not found, using default");
        return null;
    }

    private static String findEdgeBinary() {
        if (IS_WINDOWS) {
            String[] edgePaths = {
                    "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                    "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe"
            };

            for (String path : edgePaths) {
                if (new File(path).exists()) {
                    System.out.println("Found Edge binary: " + path);
                    return path;
                }
            }
        }

        System.out.println("Edge binary not found, using default");
        return null;
    }

    private static void makeExecutable(File file) {
        if (!file.canExecute()) {
            try {
                file.setExecutable(true);
                System.out.println("Made executable: " + file.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Could not make executable: " + e.getMessage());
            }
        }
    }

    private static WebDriver createWebDriver(BrowserConfig config) {
        System.out.println("\n=== CREATING WEBDRIVER ===");
        System.out.println("Browser: " + config.browserType);
        System.out.println("Driver: " + config.driverPath);
        System.out.println("Binary: " + config.binaryPath);

        if ("edge".equals(config.browserType)) {
            return createEdgeDriver(config);
        } else {
            return createChromeDriver(config);
        }
    }

    private static WebDriver createChromeDriver(BrowserConfig config) {
        // Set driver path
        System.setProperty("webdriver.chrome.driver", config.driverPath);

        ChromeOptions options = new ChromeOptions();

        // Set binary if found
        if (config.binaryPath != null) {
            options.setBinary(config.binaryPath);
        }

        // Add arguments based on architecture and environment
        options.addArguments(getChromeArguments());

        // Enable logging
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        // Create service
        ChromeDriverService.Builder serviceBuilder = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File(config.driverPath))
                .withTimeout(Duration.ofSeconds(30));

        ChromeDriverService service = serviceBuilder.build();

        return new ChromeDriver(service, options);
    }

    private static WebDriver createEdgeDriver(BrowserConfig config) {
        // Set driver path
        System.setProperty("webdriver.edge.driver", config.driverPath);

        EdgeOptions options = new EdgeOptions();

        // Set binary if found
        if (config.binaryPath != null) {
            options.setBinary(config.binaryPath);
        }

        // Add arguments based on architecture and environment
        options.addArguments(getEdgeArguments());

        // Enable logging
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        options.setCapability("ms:loggingPrefs", logPrefs);

        // Create service
        EdgeDriverService.Builder serviceBuilder = new EdgeDriverService.Builder()
                .usingDriverExecutable(new File(config.driverPath))
                .withTimeout(Duration.ofSeconds(30));

        EdgeDriverService service = serviceBuilder.build();

        return new EdgeDriver(service, options);
    }

    private static String[] getChromeArguments() {
        return getCommonBrowserArguments();
    }

    private static String[] getEdgeArguments() {
        return getCommonBrowserArguments();
    }

    private static String[] getCommonBrowserArguments() {
        String[] baseArgs = {
                "--headless=new",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--disable-extensions",
                "--disable-web-security",
                "--allow-file-access-from-files",
                "--allow-running-insecure-content",
                "--user-data-dir=/tmp/browser-test-" + System.currentTimeMillis(),
                "--disable-features=TranslateUI,VizDisplayCompositor",
                "--disable-background-timer-throttling",
                "--disable-backgrounding-occluded-windows",
                "--disable-renderer-backgrounding"
        };

        // Add ARM-specific arguments
        if (IS_ARM) {
            String[] armArgs = {
                    "--disable-features=VizDisplayCompositor",
                    "--use-gl=swiftshader",
                    "--disable-software-rasterizer"
            };

            String[] combined = new String[baseArgs.length + armArgs.length];
            System.arraycopy(baseArgs, 0, combined, 0, baseArgs.length);
            System.arraycopy(armArgs, 0, combined, baseArgs.length, armArgs.length);
            return combined;
        }

        return baseArgs;
    }

    @AfterAll
    public static void stopServer() {
        try {
            if (server != null) {
                server.stop();
                System.out.println("Javalin server stopped successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error stopping Javalin server: " + e.getMessage());
        }
    }

    private static void cleanup() {
        if (app != null) {
            app.stop();
        }
        if (driver != null) {
            try {
                driver.quit();
                driver = null;
            } catch (Exception e) {
                System.err.println("Error cleaning up WebDriver: " + e.getMessage());
            }
        }
    }

    // Helper class to store browser configuration
    private static class BrowserConfig {
        final String browserType;
        final String driverPath;
        final String binaryPath;

        BrowserConfig(String browserType, String driverPath, String binaryPath) {
            this.browserType = browserType;
            this.driverPath = driverPath;
            this.binaryPath = binaryPath;
        }
    }

    @Test
    public void displayRecipesOnInitTest() throws InterruptedException {

        // check for any issues
        handleUnexpectedAlerts(driver);

        // refresh the page to trigger backend API call
        driver.navigate().refresh();

        // gather recipe list information
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recipe-list")));
        WebElement recipeList = driver.findElement(By.id("recipe-list"));
        String innerHTML = recipeList.getAttribute("innerHTML");

        // make assertions: recipe list should contain expected recipes
        assertTrue(innerHTML.contains("carrot soup"), "Expected recipes to be displayed.");
        assertTrue(innerHTML.contains("potato soup"), "Expected recipes to be displayed.");
        assertTrue(innerHTML.contains("tomato soup"), "Expected recipes to be displayed.");
        assertTrue(innerHTML.contains("lemon rice soup"), "Expected recipes to be displayed.");
        assertTrue(innerHTML.contains("stone soup"), "Expected recipes to be displayed.");

    }

    @Test
    public void addRecipePostTest() {
        // Add a recipe
        WebElement nameInput = driver.findElement(By.id("add-recipe-name-input"));
        WebElement instructionsInput = driver.findElement(By.id("add-recipe-instructions-input"));
        WebElement addButton = driver.findElement(By.id("add-recipe-submit-input"));
        nameInput.sendKeys("Beef Stroganoff");
        instructionsInput.sendKeys("Mix beef with sauce and serve over pasta");
        addButton.click();

        // Wait for the recipe list to update
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recipe-list")));
        WebElement recipeList = driver.findElement(By.id("recipe-list"));
        String innerHTML = recipeList.getAttribute("innerHTML");

        // Assert the result
        assertTrue(innerHTML.contains("Beef Stroganoff"), "Expected recipe to be added.");

    }

    @Test
    public void updateRecipePutTest() {

        // perform update
        WebElement nameInput = driver.findElement(By.id("update-recipe-name-input"));
        WebElement instructionsInput = driver.findElement(By.id("update-recipe-instructions-input"));
        WebElement updateButton = driver.findElement(By.id("update-recipe-submit-input"));
        nameInput.sendKeys("carrot soup");
        instructionsInput.sendKeys("Updated instructions for carrot soup");
        updateButton.click();
        handleUnexpectedAlerts();

        // gather recipe list information
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("recipe-list"),
                "Updated instructions for carrot soup"));
        WebElement recipeList = driver.findElement(By.id("recipe-list"));
        String innerHTML = recipeList.getAttribute("innerHTML");

        // make assertion: recipe should be updated
        assertTrue(innerHTML.contains("Updated instructions for carrot soup"), "Expected recipe to be updated.");

    }

    @Test
    public void deleteRecipeDeleteTest() throws InterruptedException {
        // Proceed with deletion
        WebElement nameInput = driver.findElement(By.id("delete-recipe-name-input"));
        WebElement deleteButton = driver.findElement(By.id("delete-recipe-submit-input"));
        nameInput.sendKeys("stone soup");
        deleteButton.click();

        // check recipe list
        boolean recipeDeleted = wait.until(driver -> {
            WebElement recipeList = driver.findElement(By.id("recipe-list"));
            String html = recipeList.getAttribute("innerHTML");
            return !html.contains("stone soup");
        });

        // make assertion: deleted recipe should not be in list
        assertTrue(recipeDeleted, "Expected recipe to be deleted and no longer visible in the list.");

    }

    @Test
    public void searchFiltersTest() throws InterruptedException {

        WebElement searchInput = driver.findElement(By.id("search-input"));
        WebElement searchButton = driver.findElement(By.id("search-button"));
        WebElement recipeList = driver.findElement(By.id("recipe-list"));

        String searchTerm = "to soup";
        searchInput.sendKeys(searchTerm);
        searchButton.click();

        Thread.sleep(1000);
        // check recipe list
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recipe-list")));
        String innerHTML = recipeList.getAttribute("innerHTML");

        assertTrue(innerHTML.contains("potato soup"), "Expected potato soup recipe to be in list.");
        assertTrue(innerHTML.contains("tomato soup"), "Expected tomato soup recipe to be in list.");
        assertFalse(innerHTML.contains("stone soup"), "Expected stone soup recipe to NOT be in list.");
        assertFalse(innerHTML.contains("carrot soup"), "Expected carrot soup recipe to NOT be in list.");
        assertFalse(innerHTML.contains("lemon rice soup"), "Expected lemon rice soup recipe to NOT be in list.");

    }

}
