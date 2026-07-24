package com.revature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

public class AdminTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static Javalin app;
    private static JavascriptExecutor js;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AdminTest.class.getName());
    @SuppressWarnings("unused")
    private static Process httpServerProcess;
    @SuppressWarnings("unused")
    private static String browserType;

    private static Javalin server;
    private static final int PORT = 8083;

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

            js = (JavascriptExecutor) driver;

            Thread.sleep(1000);

        } catch (Exception e) {
            System.err.println("\n=== SETUP FAILED ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();

            cleanup();
            throw new RuntimeException("Setup failed", e);
        }
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

        // Check for Edge driver first
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

        // Chrome driver paths
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
                    "chromedriver.exe"
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
                    "msedgedriver.exe"
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
        System.setProperty("webdriver.chrome.driver", config.driverPath);

        ChromeOptions options = new ChromeOptions();

        if (config.binaryPath != null) {
            options.setBinary(config.binaryPath);
        }

        options.addArguments(getChromeArguments());

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        ChromeDriverService.Builder serviceBuilder = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File(config.driverPath))
                .withTimeout(Duration.ofSeconds(30));

        ChromeDriverService service = serviceBuilder.build();

        return new ChromeDriver(service, options);
    }

    private static WebDriver createEdgeDriver(BrowserConfig config) {
        System.setProperty("webdriver.edge.driver", config.driverPath);

        EdgeOptions options = new EdgeOptions();

        if (config.binaryPath != null) {
            options.setBinary(config.binaryPath);
        }

        options.addArguments(getEdgeArguments());

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        options.setCapability("ms:loggingPrefs", logPrefs);

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

    // @AfterClass
    @AfterAll
    public static void tearDown() {
        System.out.println("\n=== TEARDOWN ===");
        cleanup();
        System.out.println("Teardown completed");
    }

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

    // NEW FUNCTION

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

    private static void performLogout() {
        WebElement logoutButton = driver.findElement(By.id("logout-button"));
        logoutButton.click();
    }

    /**
     * Admin link should not exist when the logged-in user is not an admin.
     * 
     * @throws InterruptedException
     */

    @Test
    public void noAdminNoLinkTest() throws InterruptedException {

        driver.get("http://localhost:8083/login/login-page.html");

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            Alert alert = shortWait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Dismissing leftover alert: " + alert.getText());
            alert.dismiss();
        } catch (Exception ignored) {
            // No alert present — continue
        }

        WebElement usernameInput = driver.findElement(By.id("login-input"));
        WebElement passwordInput = driver.findElement(By.id("password-input"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameInput.sendKeys("JoeCool");
        passwordInput.sendKeys("redbarron");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("recipe-page"));

        // String isAdmin = (String) js.executeScript("return
        // window.sessionStorage.getItem('is-admin');");
        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return window.sessionStorage.getItem('is-admin') !== null;"));
        String isAdmin = (String) js.executeScript("return window.sessionStorage.getItem('is-admin');");

        assertEquals("false", isAdmin);

        WebElement adminLink = driver.findElement(By.id("admin-link"));
        // Assert
        Assertions.assertFalse(adminLink.isDisplayed());

        performLogout();
    }

    /**
     * Admin link should exist when the logged-in user is an admin.
     * 
     * @throws InterruptedException
     */
    /**
     * Admin link should exist when the logged-in user is an admin.
     */
    @Test
    public void adminLinkTest() throws InterruptedException {
        driver.get("http://localhost:8083/login/login-page.html");

        WebElement usernameInput = driver.findElement(By.id("login-input"));
        WebElement passwordInput = driver.findElement(By.id("password-input"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameInput.sendKeys("ChefTrevin");
        passwordInput.sendKeys("trevature");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("recipe-page"));

        // String isAdmin = (String) js.executeScript("return
        // window.sessionStorage.getItem('is-admin');");
        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return window.sessionStorage.getItem('is-admin') !== null;"));
        String isAdmin = (String) js.executeScript("return window.sessionStorage.getItem('is-admin');");
        assertEquals("true", isAdmin);

        WebElement adminLink = driver.findElement(By.id("admin-link"));
        // Assert
        Assertions.assertTrue(adminLink.isDisplayed());

        performLogout();
    }

    /**
     * On startup, the site should pull the currently available ingredients from the
     * API.
     */
    @Test
    public void displayIngredientsOnInitTest() throws InterruptedException {
        driver.get("http://localhost:8083/login/login-page.html");

        WebElement usernameInput = driver.findElement(By.id("login-input"));
        WebElement passwordInput = driver.findElement(By.id("password-input"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameInput.sendKeys("ChefTrevin");
        passwordInput.sendKeys("trevature");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("recipe-page"));
        Thread.sleep(1000);

        // driver.findElement(By.id("admin-link")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("admin-link")));
        WebElement adminLink = driver.findElement(By.id("admin-link"));
        adminLink.click();

        Thread.sleep(1000);

        WebElement list = driver.findElement(By.id("ingredient-list"));
        String innerHTML = list.getAttribute("innerHTML");

        // Assert.assertTrue(innerHTML.contains("carrot"));
        // Assert.assertTrue(innerHTML.contains("potato"));
        // Assert.assertTrue(innerHTML.contains("tomato"));
        // Assert.assertTrue(innerHTML.contains("lemon"));
        // Assert.assertTrue(innerHTML.contains("rice"));
        // Assert.assertTrue(innerHTML.contains("stone"));
        Assertions.assertTrue(innerHTML.contains("carrot"));
        Assertions.assertTrue(innerHTML.contains("potato"));
        Assertions.assertTrue(innerHTML.contains("tomato"));
        Assertions.assertTrue(innerHTML.contains("lemon"));
        Assertions.assertTrue(innerHTML.contains("rice"));
        Assertions.assertTrue(innerHTML.contains("stone"));

        driver.findElement(By.id("back-link")).click();
        Thread.sleep(1000);

        performLogout();
    }

    /**
     * The site should send a request to persist the ingredient after the recipe is
     * submitted.
     */
    @Test
    public void addIngredientPostTest() throws InterruptedException {
        driver.get("http://localhost:8083/login/login-page.html");

        WebElement usernameInput = driver.findElement(By.id("login-input"));
        WebElement passwordInput = driver.findElement(By.id("password-input"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameInput.sendKeys("ChefTrevin");
        passwordInput.sendKeys("trevature");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("recipe-page"));
        Thread.sleep(1000);

        // driver.findElement(By.id("admin-link")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("admin-link")));
        WebElement adminLink = driver.findElement(By.id("admin-link"));
        adminLink.click();

        Thread.sleep(1000);

        WebElement nameInput = driver.findElement(By.id("add-ingredient-name-input"));
        WebElement ingredientSubmitButton = driver.findElement(By.id("add-ingredient-submit-button"));

        nameInput.sendKeys("salt");
        ingredientSubmitButton.click();
        Thread.sleep(1000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ingredient-list")));
        String innerHTML = driver.findElement(By.id("ingredient-list")).getAttribute("innerHTML");
        assertTrue(innerHTML.contains("salt"), "Expected ingredient to be added.");

        driver.findElement(By.id("back-link")).click();
        Thread.sleep(1000);

        performLogout();
    }

    /**
     * The site should send a request to delete the ingredient when the delete
     * button is clicked.
     */
    @Test
    public void deleteIngredientDeleteTest() throws InterruptedException {
        driver.get("http://localhost:8083/login/login-page.html");

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            Alert alert = shortWait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Dismissing leftover alert: " + alert.getText());
            alert.dismiss();
        } catch (Exception ignored) {
        }

        WebElement usernameInput = driver.findElement(By.id("login-input"));
        WebElement passwordInput = driver.findElement(By.id("password-input"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameInput.sendKeys("ChefTrevin");
        passwordInput.sendKeys("trevature");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("recipe-page"));
        Thread.sleep(1000);

        driver.findElement(By.id("admin-link")).click();
        Thread.sleep(1000);

        WebElement nameInput = driver.findElement(By.id("delete-ingredient-name-input"));
        WebElement ingredientSubmitButton = driver.findElement(By.id("delete-ingredient-submit-button"));

        nameInput.sendKeys("tomato");
        ingredientSubmitButton.click();
        Thread.sleep(1000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ingredient-list")));
        String innerHTML = driver.findElement(By.id("ingredient-list")).getAttribute("innerHTML");
        assertFalse(innerHTML.contains("tomato"), "Expected ingredient to be deleted.");

        driver.findElement(By.id("back-link")).click();
        Thread.sleep(1000);

        performLogout();
    }

}
