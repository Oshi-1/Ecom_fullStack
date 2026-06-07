package com.ecommerce.selenium.tests;

import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.LoginPage;
import com.ecommerce.selenium.pages.RegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BasicNavigationTest extends BaseTest {

    @Test(priority = 1)
    public void rootUrlShouldNavigateToLoginPage() {
        driver.get(frontendBaseUrl + "/");

        wait.until(ExpectedConditions.urlContains("/login"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Root route should redirect users to login.");
        Assert.assertTrue(new LoginPage(driver, wait, frontendBaseUrl).isLoaded(), "Login UI should be visible.");
    }

    @Test(priority = 2)
    public void protectedDashboardShouldRedirectAnonymousUserToLogin() {
        driver.get(frontendBaseUrl + "/dashboard");

        wait.until(ExpectedConditions.urlContains("/login"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Anonymous user should be sent to login.");
    }

    @Test(priority = 3)
    public void loginPageShouldNavigateToRegistrationAndForgotPassword() {
        LoginPage loginPage = new LoginPage(driver, wait, frontendBaseUrl).open();

        loginPage.clickCreateAccount();
        Assert.assertTrue(driver.getCurrentUrl().contains("/register"), "Create account link should open register page.");
        Assert.assertTrue(new RegisterPage(driver, wait, frontendBaseUrl).isLoaded(), "Register UI should be visible.");

        loginPage.open().clickForgotPassword();
        Assert.assertTrue(driver.getCurrentUrl().contains("/forgot-password"), "Forgot password link should open forgot password page.");
        Assert.assertTrue(driver.findElement(By.xpath("//h1[normalize-space()='Reset password']")).isDisplayed(),
                "Forgot password heading should be visible.");
    }
}
