package com.ecommerce.selenium.base;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {

    private static Process frontendProcess;
    private static Process backendProcess;
    private static final String FRONTEND_HOST = "localhost";
    private static final int FRONTEND_PORT = 5173;
    private static final String BACKEND_HOST = "localhost";
    private static final int BACKEND_PORT = 8081;

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected final String frontendBaseUrl = System.getProperty(
            "frontend.baseUrl",
            "http://" + FRONTEND_HOST + ":" + FRONTEND_PORT);
    protected final String backendApiUrl = System.getProperty(
            "backend.apiUrl",
            "http://localhost:8081/api");

    @BeforeMethod
    public void setUp() {
        launchChrome();
        if (shouldStartFrontend()) {
            startFrontendIfNeeded();
        }
    }

    protected boolean shouldStartFrontend() {
        return true;
    }

    private void launchChrome() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        if (Boolean.parseBoolean(System.getProperty("selenium.keepBrowserOpen", "true"))) {
            options.setExperimentalOption("detach", true);
        }
        if (Boolean.parseBoolean(System.getProperty("selenium.headless", "false"))) {
            options.addArguments("--headless=new");
        }

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.get("about:blank");
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            if (Boolean.parseBoolean(System.getProperty("selenium.keepBrowserOpen", "true"))) {
                sleep(3);
            } else {
                driver.quit();
            }
        }
    }

    @AfterSuite(alwaysRun = true)
    public void stopStartedServers() {
        if (frontendProcess != null && frontendProcess.isAlive()) {
            frontendProcess.destroy();
        }
        if (backendProcess != null && backendProcess.isAlive()) {
            backendProcess.destroy();
        }
    }

    protected synchronized void startBackendIfNeeded() {
        if (isAvailable(backendApiUrl + "/products")) {
            return;
        }

        if (backendProcess != null && backendProcess.isAlive()) {
            waitUntilBackendStarts();
            return;
        }

        if (isPortOpen(BACKEND_HOST, BACKEND_PORT)) {
            waitUntilBackendStarts();
            return;
        }

        try {
            Path projectFolder = Paths.get(System.getProperty("user.dir"));
            File logFile = projectFolder.resolve("target").resolve("selenium-backend.log").toFile();
            logFile.getParentFile().mkdirs();

            ProcessBuilder builder = new ProcessBuilder(
                    "cmd", "/c", "mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments=-Dspring.devtools.restart.enabled=false");
            builder.directory(projectFolder.toFile());
            builder.environment().put("JAVA_HOME", System.getProperty("java.home"));
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
            backendProcess = builder.start();

            waitUntilBackendStarts();
        } catch (Exception ex) {
            throw new RuntimeException("Backend auto start nahi ho paya. MySQL running rakhein aur target/selenium-backend.log check karein.", ex);
        }
    }

    private void startFrontendIfNeeded() {
        if (isFrontendRunning()) {
            return;
        }

        try {
            Path projectFolder = Paths.get(System.getProperty("user.dir"));
            File frontendFolder = projectFolder.resolve("ecommerce-frontend").toFile();
            File logFile = projectFolder.resolve("target").resolve("selenium-frontend.log").toFile();
            logFile.getParentFile().mkdirs();

            ProcessBuilder builder = new ProcessBuilder(
                    "cmd", "/c", "npm.cmd run dev -- --host localhost --port 5173 --strictPort");
            builder.directory(frontendFolder);
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
            frontendProcess = builder.start();

            waitUntilFrontendStarts();
        } catch (Exception ex) {
            throw new RuntimeException("Frontend auto start nahi ho paya. Pehle npm install run karke dobara TestNG test run karein.", ex);
        }
    }

    private void waitUntilBackendStarts() {
        for (int i = 0; i < 90; i++) {
            if (isAvailable(backendApiUrl + "/products")) {
                return;
            }
            sleep(1);
        }

        throw new RuntimeException("Backend start nahi hua. Details ke liye target/selenium-backend.log check karein.");
    }

    private void waitUntilFrontendStarts() {
        for (int i = 0; i < 60; i++) {
            if (isFrontendRunning()) {
                return;
            }
            sleep(1);
        }

        throw new RuntimeException("Frontend start nahi hua. Details ke liye target/selenium-frontend.log check karein.");
    }

    private boolean isAvailable(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode < 500;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isFrontendRunning() {
        return isPortOpen("localhost", FRONTEND_PORT)
                || isPortOpen("127.0.0.1", FRONTEND_PORT)
                || isPortOpen("::1", FRONTEND_PORT);
    }

    private boolean isPortOpen(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(Duration.ofSeconds(seconds).toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
