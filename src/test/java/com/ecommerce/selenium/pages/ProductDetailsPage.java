package com.ecommerce.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProductDetailsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By heading = By.xpath("//h1[normalize-space()='Product Details' or normalize-space()='Admin Product Details']");
    private final By productName = By.cssSelector("section h2");
    private final By productPrice = By.xpath("//span[normalize-space()='Price']/following-sibling::strong[starts-with(normalize-space(), 'Rs.')]");
    private final By quantityInput = By.cssSelector("label input[type='number']");
    private final By addToCartButton = By.xpath("//button[normalize-space()='Add to Cart' or normalize-space()='Adding...']");
    private final By viewCartButton = By.xpath("//button[normalize-space()='View Cart']");
    private final By successToast = By.xpath("//*[contains(normalize-space(), 'added to cart')]");

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

    public ProductDetailsPage setQuantity(int quantity) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(quantityInput));
        input.clear();
        input.sendKeys(String.valueOf(quantity));
        wait.until(ExpectedConditions.attributeToBe(quantityInput, "value", String.valueOf(quantity)));
        return this;
    }

    public ProductDetailsPage addToCart() {
        clickWhenReady(addToCartButton);
        wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
        wait.until(ExpectedConditions.elementToBeClickable(addToCartButton));
        return this;
    }

    public CartPage viewCart() {
        clickWhenReady(viewCartButton);
        return new CartPage(driver, wait).waitUntilLoaded();
    }

    public boolean successMessageDisplayed() {
        return driver.findElements(successToast).stream().anyMatch(WebElement::isDisplayed);
    }

    private void clickWhenReady(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}
