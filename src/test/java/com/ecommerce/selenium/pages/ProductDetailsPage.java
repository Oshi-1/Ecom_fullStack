package com.ecommerce.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProductDetailsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By heading = By.xpath("//h1[normalize-space()='Product Details' or normalize-space()='Admin Product Details']");
    private final By productName = By.cssSelector("section h2");
    private final By productPrice = By.xpath("//span[normalize-space()='Price']/following-sibling::strong[starts-with(normalize-space(), 'Rs.')]");

    public ProductDetailsPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public ProductDetailsPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlMatches(".*/products/\\d+.*"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(heading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(productName));
        wait.until(ExpectedConditions.visibilityOfElementLocated(productPrice));
        return this;
    }

    public boolean isOpen() {
        return driver.getCurrentUrl().matches(".*/products/\\d+.*");
    }

    public String displayedProductName() {
        return driver.findElement(productName).getText().trim();
    }

    public String displayedProductPrice() {
        return driver.findElement(productPrice).getText().replace("Rs.", "").trim();
    }
}
