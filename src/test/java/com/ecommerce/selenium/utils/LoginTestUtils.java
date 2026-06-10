package com.ecommerce.selenium.utils;

import com.ecommerce.selenium.pages.DashboardPage;
import com.ecommerce.selenium.pages.LoginPage;
import com.ecommerce.selenium.pages.RegisterPage;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginTestUtils {

    public static final String TEST_USER_NAME = "Selenium Login User";
    public static final String TEST_USER_PASSWORD = "Password@123";

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String frontendBaseUrl;

    public LoginTestUtils(WebDriver driver, WebDriverWait wait, String frontendBaseUrl) {
        this.driver = driver;
        this.wait = wait;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public DashboardPage registerAndOpenDashboard(String email) {
        new RegisterPage(driver, wait, frontendBaseUrl)
                .open()
                .enterFullName(TEST_USER_NAME)
                .enterEmail(email)
                .enterPassword(TEST_USER_PASSWORD)
                .submit()
                .waitForDashboardRedirect();

        return new DashboardPage(driver, wait).waitUntilLoaded();
    }

    public DashboardPage loginAndOpenDashboard(String email) {
        new LoginPage(driver, wait, frontendBaseUrl)
                .open()
                .loginAs(email, TEST_USER_PASSWORD)
                .waitForDashboardRedirect();

        return new DashboardPage(driver, wait).waitUntilLoaded();
    }

    public String localStorageValue(String key) {
        Object value = ((JavascriptExecutor) driver).executeScript(
                "return window.localStorage.getItem(arguments[0]) || '';", key);
        return String.valueOf(value);
    }
}
