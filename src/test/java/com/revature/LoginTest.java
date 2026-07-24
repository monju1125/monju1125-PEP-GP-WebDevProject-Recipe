package com.revature;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

public class LoginTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private ClientAndServer mockServer;
    private MockServerClient mockServerClient;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(LoginTest.class.getName());
    private Process httpServerProcess;
    @SuppressWarnings("unused")
    private String browserType;
    
    // Architecture and system detection
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String OS_ARCH = System.getProperty("os.arch").toLowerCase();
    private static final boolean IS_ARM = OS_ARCH.contains("aarch64") || OS_ARCH.contains("arm");
    private static final boolean IS_WINDOWS = OS_NAME.contains("windows");
    @SuppressWarnings("unused")
    private static final boolean IS_LINUX = OS_NAME.contains("linux");
    private static final boolean IS_MAC = OS_NAME.contains("mac");

    @BeforeEach
    public void setUp() throws InterruptedException {
        try {
            printEnvironmentInfo();
            
            // 1. Detect browser and driver
            BrowserConfig browserConfig = detectBrowserAndDriver();
            this.browserType = browserConfig.browserType;
            
            // 2. Find HTML file and determine serving method
            File htmlFile = findHtmlFile();
            String htmlUrl = determineHtmlUrl(htmlFile);
            
            // 3. Create WebDriver with appropriate configuration
            driver = createWebDriver(browserConfig);
            
            // Initialize WebDriverWait
            wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            
            // Set timeouts
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            
            // Initialize MockServer
            mockServer = ClientAndServer.startClientAndServer(8081);
            mockServerClient = new MockServerClient("localhost", 8081);

            // CORS options request setup
            mockServerClient
                    .when(HttpRequest.request().withMethod("OPTIONS").withPath(".*"))
                    .respond(HttpResponse.response()
                            .withHeader("Access-Control-Allow-Origin", "*")
                            .withHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                            .withHeader("Access-Control-Allow-Headers",
                                    "Content-Type, Access-Control-Allow-Origin, Access-Control-Allow-Methods, Access-Control-Allow-Headers, Origin, Accept, X-Requested-With"));
            
            // Navigate to page
            System.out.println("\n=== NAVIGATING TO PAGE ===");
            System.out.println("Navigating to: " + htmlUrl);
            driver.get(htmlUrl);
            
            // Wait for page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            System.out.println("Page loaded successfully");
            
            printPageInfo();
            
            Thread.sleep(1000);
            
        } catch (Exception e) {
            System.err.println("\n=== SETUP FAILED ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            cleanup();
            throw new RuntimeException("Setup failed", e);
        }
    }

    private void printEnvironmentInfo() {
        System.out.println("=== ENVIRONMENT INFO ===");
        System.out.println("OS: " + OS_NAME + " (" + OS_ARCH + ")");
        System.out.println("Architecture: " + (IS_ARM ? "ARM64" : "x86/x64"));
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Working directory: " + System.getProperty("user.dir"));
    }

    private BrowserConfig detectBrowserAndDriver() {
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
    
    private BrowserConfig checkProjectDriverFolder() {
        File driverFolder = new File("driver");
        if (!driverFolder.exists() || !driverFolder.isDirectory()) {
            System.out.println("No 'driver' folder found in project root");
            return null;
        }
        
        System.out.println("Found 'driver' folder, checking for executables...");
        
        // Check for Edge driver first
        String[] edgeDriverNames = IS_WINDOWS ? 
            new String[]{"msedgedriver.exe", "edgedriver.exe"} :
            new String[]{"msedgedriver", "edgedriver"};
            
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
        String[] chromeDriverNames = IS_WINDOWS ? 
            new String[]{"chromedriver.exe"} :
            new String[]{"chromedriver"};
            
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
    
    private BrowserConfig checkSystemDrivers() {
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
            chromeDriverPaths = new String[]{
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
    
    private String findChromeBinary() {
        String[] chromePaths;
        
        if (IS_WINDOWS) {
            chromePaths = new String[]{
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
            };
        } else if (IS_MAC) {
            chromePaths = new String[]{
                "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
            };
        } else {
            chromePaths = new String[]{
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
    
    private String findEdgeBinary() {
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
    
    private void makeExecutable(File file) {
        if (!file.canExecute()) {
            try {
                file.setExecutable(true);
                System.out.println("Made executable: " + file.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Could not make executable: " + e.getMessage());
            }
        }
    }
    
    private File findHtmlFile() {
        String[] possibleHtmlPaths = {
            "src/main/resources/public/frontend/login/login-page.html",
            "login-page.html",
            "src/test/resources/login-page.html",
            "test-resources/login-page.html",
            "src/main/resources/login-page.html"
        };
        
        for (String htmlPath : possibleHtmlPaths) {
            File testFile = new File(htmlPath);
            if (testFile.exists()) {
                System.out.println("Found HTML file: " + testFile.getAbsolutePath());
                return testFile;
            }
        }
        
        throw new RuntimeException("Could not find login-page.html in any expected location: " + 
            Arrays.toString(possibleHtmlPaths));
    }
    
    private String determineHtmlUrl(File htmlFile) {
        // Try to use HTTP server first if Python3 is available
        if (isPython3Available()) {
            try {
                return startHttpServer(htmlFile);
            } catch (Exception e) {
                System.out.println("HTTP server failed, falling back to file URL: " + e.getMessage());
            }
        } else {
            System.out.println("Python3 not available, using file URL");
        }
        
        // Fallback to file URL
        return "file://" + htmlFile.getAbsolutePath();
    }
    
    private boolean isPython3Available() {
        try {
            Process process = new ProcessBuilder("python3", "--version").start();
            boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            if (finished && process.exitValue() == 0) {
                System.out.println("Python3 is available");
                return true;
            }
        } catch (Exception e) {
            // Ignore
        }
        
        // Also try "python" on Windows
        if (IS_WINDOWS) {
            try {
                Process process = new ProcessBuilder("python", "--version").start();
                boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                if (finished && process.exitValue() == 0) {
                    System.out.println("Python is available");
                    return true;
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        System.out.println("Python3/Python not available");
        return false;
    }
    
    @SuppressWarnings("deprecation")
    private String startHttpServer(File htmlFile) throws Exception {
        int port = 8000 + (int)(Math.random() * 1000);
        String directory = htmlFile.getParent();
        String fileName = htmlFile.getName();
        
        System.out.println("Starting HTTP server on port " + port);
        
        String pythonCmd = IS_WINDOWS ? "python" : "python3";
        ProcessBuilder pb = new ProcessBuilder(pythonCmd, "-m", "http.server", String.valueOf(port));
        pb.directory(new File(directory));
        pb.redirectErrorStream(true);
        
        httpServerProcess = pb.start();
        
        // Wait for server to start
        Thread.sleep(3000);
        
        if (!httpServerProcess.isAlive()) {
            throw new RuntimeException("HTTP server failed to start");
        }
        
        String url = "http://localhost:" + port + "/" + fileName;
        
        // Test connectivity
        for (int i = 0; i < 10; i++) {
            try {
                java.net.URL testUrl = new java.net.URL(url);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) testUrl.openConnection();
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                connection.disconnect();
                
                if (responseCode == 200) {
                    System.out.println("HTTP server ready: " + url);
                    return url;
                }
            } catch (Exception e) {
                if (i == 9) {
                    throw new RuntimeException("HTTP server not responding: " + e.getMessage());
                }
                Thread.sleep(1000);
            }
        }
        
        throw new RuntimeException("HTTP server failed to respond");
    }
    
    private WebDriver createWebDriver(BrowserConfig config) {
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
    
    private WebDriver createChromeDriver(BrowserConfig config) {
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
    
    private WebDriver createEdgeDriver(BrowserConfig config) {
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
    
    private String[] getChromeArguments() {
        return getCommonBrowserArguments();
    }
    
    private String[] getEdgeArguments() {
        return getCommonBrowserArguments();
    }
    
    private String[] getCommonBrowserArguments() {
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
    
    private void printPageInfo() {
        System.out.println("Page title: " + driver.getTitle());
        System.out.println("Current URL: " + driver.getCurrentUrl());
        System.out.println("Page source length: " + driver.getPageSource().length());
    }
    
    private void stopHttpServer() {
        if (httpServerProcess != null) {
            try {
                System.out.println("Stopping HTTP server...");
                httpServerProcess.destroy();
                
                boolean terminated = httpServerProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                if (!terminated) {
                    httpServerProcess.destroyForcibly();
                }
                
                httpServerProcess = null;
                System.out.println("HTTP server stopped");
            } catch (Exception e) {
                System.out.println("Warning: Error stopping HTTP server: " + e.getMessage());
                try {
                    httpServerProcess.destroyForcibly();
                } catch (Exception ignored) {}
                httpServerProcess = null;
            }
        }
    }
    
    private void cleanup() {
        stopHttpServer();
        if (driver != null) {
            try {
                driver.quit();
                driver = null;
            } catch (Exception e) {
                System.err.println("Error cleaning up WebDriver: " + e.getMessage());
            }
        }
        if (mockServer != null) {
            try {
                mockServer.stop();
            } catch (Exception e) {
                System.err.println("Error stopping MockServer: " + e.getMessage());
            }
        }
        if (mockServerClient != null) {
            try {
                mockServerClient.close();
            } catch (Exception e) {
                System.err.println("Error closing MockServerClient: " + e.getMessage());
            }
        }
    }

    @Test
    public void correctLoginTest() throws InterruptedException {
        // Locate elements using the correct IDs from the HTML
        WebElement nameInput = driver.findElement(By.id("login-input"));
        WebElement passwordInput = driver.findElement(By.id("password-input"));
        WebElement submitButton = driver.findElement(By.id("login-button"));

        // Mocking the login response
        mockServerClient
                .when(HttpRequest.request().withMethod("POST").withPath("/login"))
                .respond(HttpResponse.response()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Access-Control-Allow-Origin", "*")
                        .withBody("{\"auth-token\":\"12345\"}"));

        nameInput.sendKeys("correct");
        passwordInput.sendKeys("correct");
        submitButton.click();
        Thread.sleep(1000);

        // Dismiss any unexpected alert and print the alert text
        try {
            String alertText = driver.switchTo().alert().getText();
            System.out.println("Alert Text: " + alertText); // Print alert text for debugging
            driver.switchTo().alert().accept(); // Dismiss the alert
        } catch (org.openqa.selenium.NoAlertPresentException e) {
            System.out.println("No alert present after clicking the login button.");
        }

        // Assertion to verify redirection to the recipe page
        assertTrue("URL should contain 'recipe-page.html' after successful login.",
                driver.getCurrentUrl().contains("recipe-page.html"));
    }

    @Test
    public void incorrectLoginTest() throws InterruptedException {
        // Locate elements using the correct IDs from the HTML
        WebElement nameInput = driver.findElement(By.id("login-input"));
        WebElement passwordInput = driver.findElement(By.id("password-input"));
        WebElement submitButton = driver.findElement(By.id("login-button"));

        // Mocking the unauthorized response
        mockServerClient
                .when(HttpRequest.request().withMethod("POST").withPath("/login"))
                .respond(HttpResponse.response()
                        .withStatusCode(401)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Access-Control-Allow-Origin", "*"));

        nameInput.sendKeys("incorrect");
        passwordInput.sendKeys("incorrect");
        submitButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().dismiss();

        Thread.sleep(1000);
        assertTrue(driver.getCurrentUrl().contains("login"));
    }

    // @After
    @AfterEach
    public void tearDown() {
        System.out.println("\n=== TEARDOWN ===");
        cleanup();
        System.out.println("Teardown completed");
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
}
