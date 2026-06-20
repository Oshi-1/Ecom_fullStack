package com.ecommerce.selenium.pages;

import java.math.BigDecimal;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CartPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By heading = By.xpath("//h1[normalize-space()='Cart']");
    private final By loadingMessage = By.xpath("//*[normalize-space()='Loading...']");
    private final By cartItems = By.cssSelector("article");
    private final By emptyCartMessage = By.xpath("//*[normalize-space()='Your cart is empty']");
    private final By summaryItems = By.xpath("//aside//span[normalize-space()='Items']/following-sibling::strong");
    private final By summaryTotal = By.xpath("//aside//span[normalize-space()='Total']/following-sibling::strong");
    private final By notification = By.cssSelector("[data-testid='app-notification']");

    public CartPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public CartPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("/cart"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(heading));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingMessage));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.numberOfElementsToBeMoreThan(cartItems, 0),
                ExpectedConditions.visibilityOfElementLocated(emptyCartMessage)));
        return this;
    }

    public boolean isOpen() {
        return driver.getCurrentUrl().contains("/cart");
    }

    public boolean hasProduct(String productName) {
        return !driver.findElements(itemByProductName(productName)).isEmpty();
    }

    public int quantityForProduct(String productName) {
        WebElement quantity = wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                cartItem(productName), By.xpath(".//div[starts-with(@aria-label, 'Quantity for ')]/span"))).get(0);
        return Integer.parseInt(quantity.getText().trim());
    }

    public CartPage increaseQuantityForProduct(String productName) {
        int currentQuantity = quantityForProduct(productName);
        clickQuantityButton(productName, "Increase");
        wait.until(driver -> quantityForProduct(productName) == currentQuantity + 1);
        waitForQuantityUpdateSuccess();
        return this;
    }

    public CartPage decreaseQuantityForProduct(String productName) {
        int currentQuantity = quantityForProduct(productName);
        clickQuantityButton(productName, "Decrease");
        wait.until(driver -> quantityForProduct(productName) == currentQuantity - 1);
        waitForQuantityUpdateSuccess();
        return this;
    }

    public boolean isDecreaseDisabledForProduct(String productName) {
        return !quantityButton(productName, "Decrease").isEnabled();
    }

    public boolean quantityUpdateErrorDisplayed() {
        return driver.findElements(notification).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(element -> element.getText().contains("Quantity could not be updated"));
    }

    public CartPage waitForSubtotal(String productName, BigDecimal expectedSubtotal) {
        wait.until(driver -> subtotalForProduct(productName).compareTo(expectedSubtotal.stripTrailingZeros()) == 0);
        return this;
    }

    public CartPage waitForSummary(int expectedItems, BigDecimal expectedTotal) {
        wait.until(driver -> totalItems() == expectedItems
                && totalAmount().compareTo(expectedTotal.stripTrailingZeros()) == 0);
        return this;
    }

    public BigDecimal priceForProduct(String productName) {
        return priceAtIndex(productName, 1);
    }

    public BigDecimal subtotalForProduct(String productName) {
        return priceAtIndex(productName, 2);
    }

    public int totalItems() {
        return Integer.parseInt(wait.until(ExpectedConditions.visibilityOfElementLocated(summaryItems)).getText().trim());
    }

    public BigDecimal totalAmount() {
        return normalizePrice(wait.until(ExpectedConditions.visibilityOfElementLocated(summaryTotal)).getText());
    }

    private BigDecimal priceAtIndex(String productName, int index) {
        WebElement item = wait.until(ExpectedConditions.visibilityOfElementLocated(itemByProductName(productName)));
        return normalizePrice(item.findElements(By.xpath(".//strong[starts-with(normalize-space(), 'Rs.')]"))
                .get(index - 1)
                .getText());
    }

    private By cartItem(String productName) {
        return itemByProductName(productName);
    }

    private void clickQuantityButton(String productName, String action) {
        closeNotificationIfVisible();
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(quantityButtonByProduct(productName, action)));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", button);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
    }

    private WebElement quantityButton(String productName, String action) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(quantityButtonByProduct(productName, action)));
    }

    private By quantityButtonByProduct(String productName, String action) {
        return By.xpath("//article[.//h3[normalize-space()=" + xpathLiteral(productName) + "]]"
                + "//button[starts-with(@aria-label, '" + action + "')]");
    }

    private void waitForQuantityUpdateSuccess() {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(notification, "Cart quantity updated."));
    }

    private void closeNotificationIfVisible() {
        driver.findElements(By.cssSelector("[data-testid='app-notification'] button"))
                .stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .ifPresent(button -> ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button));
    }

    private By itemByProductName(String productName) {
        return By.xpath("//article[.//h3[normalize-space()=" + xpathLiteral(productName) + "]]");
    }

    private BigDecimal normalizePrice(String priceText) {
        return new BigDecimal(priceText.replace("Rs.", "").trim()).stripTrailingZeros();
    }

    private String xpathLiteral(String text) {
        if (!text.contains("'")) {
            return "'" + text + "'";
        }
        if (!text.contains("\"")) {
            return "\"" + text + "\"";
        }

        String[] parts = text.split("'");
        StringBuilder literal = new StringBuilder("concat(");
        for (int index = 0; index < parts.length; index++) {
            if (index > 0) {
                literal.append(", \"'\", ");
            }
            literal.append("'").append(parts[index]).append("'");
        }
        literal.append(")");
        return literal.toString();
    }
}
