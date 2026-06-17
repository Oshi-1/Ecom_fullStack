package com.ecommerce.selenium.pages;

import java.math.BigDecimal;
import org.openqa.selenium.By;
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
                cartItem(productName), By.cssSelector("input[type='number']"))).get(0);
        return Integer.parseInt(quantity.getDomAttribute("value"));
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
