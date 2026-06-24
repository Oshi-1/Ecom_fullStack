package com.ecommerce.selenium.pages;

import java.math.BigDecimal;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CheckoutPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By heading = By.xpath("//h1[normalize-space()='Checkout']");
    private final By loadingMessage = By.xpath("//*[normalize-space()='Loading checkout...']");
    private final By checkoutItems = By.cssSelector("[class*='checkoutItem']");
    private final By emptyCartMessage = By.xpath("//*[normalize-space()='Your cart is empty']");
    private final By houseNoInput = By.name("houseNo");
    private final By streetInput = By.name("street");
    private final By cityInput = By.name("city");
    private final By pincodeInput = By.name("pincode");
    private final By stateInput = By.name("state");
    private final By contactNumberInput = By.name("contactNumber");
    private final By notification = By.cssSelector("[data-testid='app-notification']");
    private final By contactNumberError = By.xpath("//*[normalize-space()='Enter a valid contact number']");
    private final By placeOrderButton = By.xpath("//button[normalize-space()='Place Order' or normalize-space()='Placing Order...']");
    private final By summaryItems = By.xpath("//aside//span[normalize-space()='Items']/following-sibling::strong");
    private final By summaryTotal = By.xpath("//aside//span[normalize-space()='Total']/following-sibling::strong");
    private final By orderSuccessHeading = By.xpath("//h1[normalize-space()='Thank you for your order']");
    private final By successAddress = By.cssSelector("[class*='successAddress'] p");
    private final By successPayment = By.xpath("//*[normalize-space()='Payment']/following-sibling::strong");

    public CheckoutPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public CheckoutPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("/checkout"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(heading));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingMessage));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.numberOfElementsToBeMoreThan(checkoutItems, 0),
                ExpectedConditions.visibilityOfElementLocated(emptyCartMessage)));
        return this;
    }

    public boolean isOpen() {
        return driver.getCurrentUrl().contains("/checkout");
    }

    public boolean hasOrderSummaryForProduct(String productName) {
        return driver.findElements(By.xpath("//aside//*[normalize-space()=" + xpathLiteral(productName) + "]"))
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }

    public String summaryProductName(String productName) {
        return summaryItem(productName)
                .findElement(By.xpath(".//strong[normalize-space()=" + xpathLiteral(productName) + "]"))
                .getText()
                .trim();
    }

    public int summaryQuantityForProduct(String productName) {
        String quantityPriceText = summaryItem(productName)
                .findElement(By.xpath(".//span[contains(normalize-space(), ' x Rs. ')]"))
                .getText()
                .trim();
        return Integer.parseInt(quantityPriceText.split(" x Rs\\. ")[0].trim());
    }

    public BigDecimal summaryUnitPriceForProduct(String productName) {
        String quantityPriceText = summaryItem(productName)
                .findElement(By.xpath(".//span[contains(normalize-space(), ' x Rs. ')]"))
                .getText()
                .trim();
        return new BigDecimal(quantityPriceText.split(" x Rs\\. ")[1].trim()).stripTrailingZeros();
    }

    public BigDecimal summarySubtotalForProduct(String productName) {
        return normalizePrice(summaryItem(productName)
                .findElement(By.xpath("./b[starts-with(normalize-space(), 'Rs.')]"))
                .getText());
    }

    public int totalItems() {
        return Integer.parseInt(wait.until(ExpectedConditions.visibilityOfElementLocated(summaryItems)).getText().trim());
    }

    public BigDecimal totalAmount() {
        return normalizePrice(wait.until(ExpectedConditions.visibilityOfElementLocated(summaryTotal)).getText());
    }

    public boolean addressFieldsDisplayed() {
        return isDisplayed(houseNoInput)
                && isDisplayed(streetInput)
                && isDisplayed(cityInput)
                && isDisplayed(pincodeInput)
                && isDisplayed(stateInput)
                && isDisplayed(contactNumberInput);
    }

    public boolean isCashOnDeliverySelected() {
        return driver.findElement(By.cssSelector("input[name='paymentMethod'][value='COD']")).isSelected();
    }

    public boolean isPaymentMethodDisplayed(String paymentMethod) {
        return driver.findElements(By.cssSelector("input[name='paymentMethod'][value='" + paymentMethod + "']"))
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }

    public boolean isPaymentMethodSelected(String paymentMethod) {
        return driver.findElement(By.cssSelector("input[name='paymentMethod'][value='" + paymentMethod + "']")).isSelected();
    }

    public boolean isPlaceOrderEnabled() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(placeOrderButton)).isEnabled();
    }

    public CheckoutPage enterHouseNo(String value) {
        type(houseNoInput, value);
        return this;
    }

    public CheckoutPage enterStreet(String value) {
        type(streetInput, value);
        return this;
    }

    public CheckoutPage enterCity(String value) {
        type(cityInput, value);
        return this;
    }

    public CheckoutPage enterPincode(String value) {
        type(pincodeInput, value);
        return this;
    }

    public CheckoutPage enterState(String value) {
        type(stateInput, value);
        return this;
    }

    public CheckoutPage enterContactNumber(String value) {
        type(contactNumberInput, value);
        return this;
    }

    public CheckoutPage enterValidCheckoutDetails() {
        return enterHouseNo("221B")
                .enterStreet("Baker Street")
                .enterCity("Bengaluru")
                .enterPincode("560001")
                .enterState("Karnataka")
                .enterContactNumber("9876543210");
    }

    public CheckoutPage clearField(String fieldName) {
        type(inputByName(fieldName), "");
        return this;
    }

    public CheckoutPage selectPaymentMethod(String paymentMethod) {
        clickWhenReady(By.cssSelector("input[name='paymentMethod'][value='" + paymentMethod + "']"));
        wait.until(driver -> driver.findElement(By.cssSelector("input[name='paymentMethod'][value='" + paymentMethod + "']")).isSelected());
        return this;
    }

    public CheckoutPage submitExpectingValidationBlock() {
        clickWhenReady(placeOrderButton);
        wait.until(ExpectedConditions.urlContains("/checkout"));
        return this;
    }

    public CheckoutPage submitOrderSuccessfully() {
        clickWhenReady(placeOrderButton);
        wait.until(ExpectedConditions.urlContains("/order-success"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(orderSuccessHeading));
        return this;
    }

    public boolean pincodeInputValid() {
        return isInputValid(pincodeInput);
    }

    public boolean contactNumberInputValid() {
        return isInputValid(contactNumberInput);
    }

    public boolean contactNumberErrorDisplayed() {
        return driver.findElements(contactNumberError).stream().anyMatch(WebElement::isDisplayed);
    }

    public boolean validationPopupDisplayed(String expectedMessage) {
        return wait.until(driver -> driver.findElements(notification).stream()
                .filter(WebElement::isDisplayed)
                .anyMatch(element -> element.getText().contains(expectedMessage)));
    }

    public boolean orderSuccessDisplayed() {
        return driver.getCurrentUrl().contains("/order-success")
                && driver.findElements(orderSuccessHeading).stream().anyMatch(WebElement::isDisplayed);
    }

    public boolean successAddressContains(String expectedText) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successAddress)).getText().contains(expectedText);
    }

    public boolean successPaymentMethodIs(String expectedPaymentMethod) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successPayment))
                .getText()
                .trim()
                .equals(expectedPaymentMethod);
    }

    private WebElement summaryItem(String productName) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//aside//div[.//strong[normalize-space()=" + xpathLiteral(productName)
                        + "] and .//span[contains(normalize-space(), ' x Rs. ')] and ./b[starts-with(normalize-space(), 'Rs.')]]")));
    }

    private boolean isDisplayed(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    private boolean isInputValid(By locator) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript("return arguments[0].checkValidity();", input));
    }

    private void type(By locator, String value) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(locator));
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.BACK_SPACE);
        if (!value.isEmpty()) {
            input.sendKeys(value);
        }
        wait.until(driver -> value.equals(inputValue(locator)));
    }

    private By inputByName(String fieldName) {
        return By.name(fieldName);
    }

    private String inputValue(By locator) {
        String value = driver.findElement(locator).getAttribute("value");
        return value == null ? "" : value;
    }

    private void clickWhenReady(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
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
