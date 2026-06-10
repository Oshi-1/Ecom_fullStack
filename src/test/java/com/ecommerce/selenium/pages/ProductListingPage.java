package com.ecommerce.selenium.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProductListingPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By pageHeading = By.cssSelector("main h1, h1");
    private final By searchInput = By.cssSelector("input[placeholder='Search anything...']");
    private final By categorySelect = By.cssSelector("select");
    private final By searchButton = By.xpath("//button[normalize-space()='Search']");
    private final By resetButton = By.xpath("//button[normalize-space()='Reset']");
    private final By cartButton = By.xpath("//button[normalize-space()='Cart']");
    private final By backButton = By.xpath("//button[normalize-space()='Back']");
    private final By productCards = By.cssSelector("article[role='button']");
    private final By loadingMessage = By.xpath("//*[normalize-space()='Loading...']");
    private final By emptyMessage = By.xpath("//*[normalize-space()='No products found']");
    private final By productName = By.cssSelector("h3");
    private final By productImage = By.cssSelector("img");
    private final By productPrice = By.xpath(".//span[starts-with(normalize-space(), 'Rs.')]");

    public ProductListingPage(WebDriver driver, WebDriverWait wait, String baseUrl) {
        this.driver = driver;
        this.wait = wait;
        this.baseUrl = baseUrl;
    }

    public ProductListingPage open() {
        driver.get(baseUrl + "/products");
        return waitUntilLoaded();
    }

    public ProductListingPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("/products"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
        waitForLoadingToFinish();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.numberOfElementsToBeMoreThan(productCards, 0),
                ExpectedConditions.visibilityOfElementLocated(emptyMessage)));
        return this;
    }

    public boolean isLoaded() {
        return driver.getCurrentUrl().contains("/products")
                && driver.findElement(pageHeading).isDisplayed()
                && driver.findElement(searchInput).isDisplayed()
                && driver.findElement(categorySelect).isDisplayed()
                && driver.findElement(searchButton).isDisplayed()
                && driver.findElement(resetButton).isDisplayed()
                && driver.findElement(cartButton).isDisplayed()
                && driver.findElement(backButton).isDisplayed();
    }

    public boolean isProductListingPageOpen() {
        return driver.getCurrentUrl().contains("/products");
    }

    public boolean isProductListingSectionDisplayed() {
        return driver.findElements(productCards).stream().anyMatch(WebElement::isDisplayed);
    }

    public boolean allListedProductsVisible() {
        return totalProductCount() > 0 && hiddenProductCount() == 0;
    }

    public boolean allProductNamesVisible() {
        return allProductsHaveVisibleElement(productName, true);
    }

    public boolean allProductImagesVisible() {
        return allProductsHaveVisibleElement(productImage, false);
    }

    public boolean allProductPricesVisible() {
        return allProductsHaveVisibleElement(productPrice, true);
    }

    public int visibleProductCount() {
        return visibleProductCards().size();
    }

    public int totalProductCount() {
        return driver.findElements(productCards).size();
    }

    public int hiddenProductCount() {
        return totalProductCount() - visibleProductCount();
    }

    public Set<String> displayedProductNames() {
        return driver.findElements(productCards).stream()
                .map(card -> card.findElement(productName).getText().trim())
                .collect(Collectors.toSet());
    }

    public List<String> scrollThroughAndValidateAllProducts() {
        List<WebElement> cards = driver.findElements(productCards);
        List<String> validationErrors = new ArrayList<>();

        for (int index = 0; index < cards.size(); index++) {
            WebElement card = cards.get(index);
            scrollIntoView(card);
            wait.until(ExpectedConditions.visibilityOf(card));

            List<WebElement> names = card.findElements(productName);
            String cardLabel = "Product card #" + (index + 1);
            if (names.isEmpty() || !names.get(0).isDisplayed() || names.get(0).getText().trim().isEmpty()) {
                validationErrors.add(cardLabel + " ka name hidden ya missing hai");
            } else {
                cardLabel = "Product '" + names.get(0).getText().trim() + "'";
            }

            List<WebElement> images = card.findElements(productImage);
            if (images.isEmpty() || !images.get(0).isDisplayed()
                    || images.get(0).getDomAttribute("src") == null
                    || images.get(0).getDomAttribute("src").isBlank()) {
                validationErrors.add(cardLabel + " ki image hidden ya missing hai");
            }

            List<WebElement> prices = card.findElements(productPrice);
            if (prices.isEmpty() || !prices.get(0).isDisplayed()
                    || prices.get(0).getText().replace("Rs.", "").trim().isEmpty()) {
                validationErrors.add(cardLabel + " ka price hidden ya missing hai");
            }
        }

        scrollToPageBottom();
        return validationErrors;
    }

    private List<WebElement> visibleProductCards() {
        return driver.findElements(productCards).stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    private boolean allProductsHaveVisibleElement(By locator, boolean requireText) {
        return totalProductCount() > 0 && driver.findElements(productCards).stream().allMatch(card -> {
            if (!card.isDisplayed()) {
                return false;
            }

            var elements = card.findElements(locator);
            if (elements.isEmpty() || !elements.get(0).isDisplayed()) {
                return false;
            }

            WebElement element = elements.get(0);
            if (locator.equals(productImage)) {
                String source = element.getDomAttribute("src");
                return source != null && !source.isBlank();
            }

            return !requireText || !element.getText().trim().isEmpty();
        });
    }

    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        pauseForVisibleScroll();
    }

    private void scrollToPageBottom() {
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});");
        pauseForVisibleScroll();
    }

    private void pauseForVisibleScroll() {
        try {
            Thread.sleep(Duration.ofMillis(700).toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForLoadingToFinish() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingMessage));
    }
}
