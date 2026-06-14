package com.ecommerce.selenium.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProductListingPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By pageHeading = By.cssSelector("main h1, h1");
    private final By searchInput = By.cssSelector("input[placeholder='Search anything...']");
    private final By categorySelect = By.cssSelector("select[aria-label='Category']");
    private final By brandSelect = By.cssSelector("select[aria-label='Brand']");
    private final By minPriceInput = By.cssSelector("input[aria-label='Minimum price']");
    private final By maxPriceInput = By.cssSelector("input[aria-label='Maximum price']");
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
    private final By productBrand = By.cssSelector("[data-testid='product-brand']");
    private final By nextPageButton = By.xpath("//nav[@aria-label='Product pagination']//button[normalize-space()='Next']");
    private final By previousPageButton = By.xpath("//nav[@aria-label='Product pagination']//button[normalize-space()='Previous']");
    private final By pageIndicator = By.cssSelector("nav[aria-label='Product pagination'] span");

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
                && driver.findElement(brandSelect).isDisplayed()
                && driver.findElement(minPriceInput).isDisplayed()
                && driver.findElement(maxPriceInput).isDisplayed()
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

    public Set<String> displayedProductCategories() {
        return driver.findElements(productCards).stream()
                .map(card -> card.findElement(By.cssSelector("p")).getText().trim())
                .collect(Collectors.toSet());
    }

    public Set<String> displayedProductBrands() {
        return driver.findElements(productCards).stream()
                .map(card -> card.findElement(productBrand).getText().trim())
                .collect(Collectors.toSet());
    }

    public List<String> availableCategories() {
        return new Select(driver.findElement(categorySelect))
                .getOptions()
                .stream()
                .map(option -> option.getText().trim())
                .filter(option -> !option.equalsIgnoreCase("All"))
                .toList();
    }

    public List<String> availableBrands() {
        return new Select(driver.findElement(brandSelect))
                .getOptions()
                .stream()
                .map(option -> option.getText().trim())
                .filter(option -> !option.equalsIgnoreCase("All Brands"))
                .toList();
    }

    public Map<String, String> displayedProductNamePrices() {
        Map<String, String> productNamePrices = new LinkedHashMap<>();

        for (WebElement card : driver.findElements(productCards)) {
            productNamePrices.put(
                    card.findElement(productName).getText().trim(),
                    normalizePriceText(card.findElement(productPrice).getText()));
        }

        return productNamePrices;
    }

    public Set<String> allProductNamesAcrossPages() {
        Set<String> names = new java.util.LinkedHashSet<>();
        returnToFirstPage();

        while (true) {
            names.addAll(displayedProductNames());
            if (!isNextPageEnabled()) {
                returnToFirstPage();
                return names;
            }
            goToNextPage();
        }
    }

    public Map<String, String> allProductNamePricesAcrossPages() {
        Map<String, String> productNamePrices = new LinkedHashMap<>();
        returnToFirstPage();

        while (true) {
            productNamePrices.putAll(displayedProductNamePrices());
            if (!isNextPageEnabled()) {
                returnToFirstPage();
                return productNamePrices;
            }
            goToNextPage();
        }
    }

    public String firstProductName() {
        return firstProductCard().findElement(productName).getText().trim();
    }

    public String firstProductPrice() {
        return normalizePriceText(firstProductCard().findElement(productPrice).getText());
    }

    public ProductListingPage searchFor(String keyword) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        input.clear();
        input.sendKeys(keyword);
        wait.until(ExpectedConditions.attributeToBe(searchInput, "value", keyword));
        clickWhenReady(searchButton);
        waitForLoadingToFinish();
        waitForResultsOrEmptyMessage();
        return this;
    }

    public ProductListingPage filterByCategory(String category) {
        WebElement selectElement = wait.until(ExpectedConditions.elementToBeClickable(categorySelect));
        new Select(selectElement).selectByVisibleText(category);
        wait.until(driver -> selectedCategory().equals(category));
        waitForResultsOrEmptyMessage();
        return this;
    }

    public ProductListingPage filterByBrand(String brand) {
        Set<String> previousNames = displayedProductNames();
        WebElement selectElement = wait.until(ExpectedConditions.elementToBeClickable(brandSelect));
        new Select(selectElement).selectByVisibleText(brand);
        wait.until(driver -> selectedBrand().equals(brand));
        waitForResultChangeOrStableState(previousNames);
        return this;
    }

    public ProductListingPage filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        setInputValue(minPriceInput, minPrice.toPlainString());
        setInputValue(maxPriceInput, maxPrice.toPlainString());
        clickWhenReady(searchButton);
        waitForLoadingToFinish();
        wait.until(driver -> isEmptyMessageDisplayed() || allProductsAreWithinPriceRange(minPrice, maxPrice));
        return this;
    }

    public ProductListingPage resetFilters() {
        clickWhenReady(resetButton);
        wait.until(ExpectedConditions.attributeToBe(searchInput, "value", ""));
        wait.until(driver -> selectedCategory().equals(""));
        wait.until(driver -> selectedBrand().equals(""));
        wait.until(driver -> minPriceValue().isEmpty() && maxPriceValue().isEmpty());
        waitForResultsOrEmptyMessage();
        return this;
    }

    public boolean isEmptyMessageDisplayed() {
        return driver.findElements(emptyMessage).stream().anyMatch(WebElement::isDisplayed);
    }

    public String searchKeywordValue() {
        return driver.findElement(searchInput).getDomAttribute("value");
    }

    public String selectedCategory() {
        return new Select(driver.findElement(categorySelect)).getFirstSelectedOption().getDomAttribute("value");
    }

    public String selectedBrand() {
        return new Select(driver.findElement(brandSelect)).getFirstSelectedOption().getDomAttribute("value");
    }

    public String minPriceValue() {
        return driver.findElement(minPriceInput).getDomAttribute("value");
    }

    public String maxPriceValue() {
        return driver.findElement(maxPriceInput).getDomAttribute("value");
    }

    public boolean allProductsContainKeyword(String keyword) {
        String normalizedKeyword = keyword.toLowerCase();
        return totalProductCount() > 0 && driver.findElements(productCards).stream()
                .map(card -> card.findElement(productName).getText().toLowerCase())
                .allMatch(name -> name.contains(normalizedKeyword));
    }

    public boolean allProductsAreInCategory(String expectedCategory) {
        return totalProductCount() > 0 && displayedProductCategories().stream()
                .allMatch(category -> category.equalsIgnoreCase(expectedCategory));
    }

    public boolean allProductsAreInBrand(String expectedBrand) {
        return totalProductCount() > 0 && displayedProductBrands().stream()
                .allMatch(brand -> brand.equalsIgnoreCase(expectedBrand));
    }

    public boolean allProductsAreWithinPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return totalProductCount() > 0 && driver.findElements(productCards).stream()
                .map(card -> priceValue(card.findElement(productPrice).getText()))
                .allMatch(price -> price.compareTo(minPrice) >= 0 && price.compareTo(maxPrice) <= 0);
    }

    public ProductListingPage waitUntilProductNamesAre(Set<String> expectedNames) {
        wait.until(driver -> displayedProductNames().equals(expectedNames));
        return this;
    }

    public boolean hasPagination() {
        return driver.findElements(nextPageButton).stream().anyMatch(WebElement::isDisplayed);
    }

    public String pageIndicatorText() {
        return driver.findElement(pageIndicator).getText().trim();
    }

    public ProductListingPage goToNextPage() {
        Set<String> previousNames = displayedProductNames();
        clickWhenReady(nextPageButton);
        wait.until(driver -> !displayedProductNames().equals(previousNames));
        return this;
    }

    public ProductListingPage goToPreviousPage() {
        Set<String> previousNames = displayedProductNames();
        clickWhenReady(previousPageButton);
        wait.until(driver -> !displayedProductNames().equals(previousNames));
        return this;
    }

    public ProductListingPage returnToFirstPage() {
        while (isPreviousPageEnabled()) {
            goToPreviousPage();
        }
        return this;
    }

    public void openFirstProductDetails() {
        WebElement firstCard = firstProductCard();
        scrollIntoView(firstCard);
        wait.until(ExpectedConditions.visibilityOf(firstCard));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstCard);
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

    private WebElement firstProductCard() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(productCards, 0));
        return driver.findElements(productCards).get(0);
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

    private void waitForResultsOrEmptyMessage() {
        wait.until(ExpectedConditions.or(
                ExpectedConditions.numberOfElementsToBeMoreThan(productCards, 0),
                ExpectedConditions.visibilityOfElementLocated(emptyMessage)));
    }

    private void waitForResultChangeOrStableState(Set<String> previousNames) {
        wait.until(driver -> !displayedProductNames().equals(previousNames)
                || totalProductCount() > 0
                || isEmptyMessageDisplayed());
    }

    private String normalizePriceText(String priceText) {
        return priceText.replace("Rs.", "").trim();
    }

    private BigDecimal priceValue(String priceText) {
        return new BigDecimal(normalizePriceText(priceText)).stripTrailingZeros();
    }

    private void setInputValue(By locator, String value) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(locator));
        input.clear();
        input.sendKeys(value);
    }

    private boolean isNextPageEnabled() {
        return driver.findElements(nextPageButton).stream()
                .anyMatch(button -> button.isDisplayed() && button.isEnabled());
    }

    private boolean isPreviousPageEnabled() {
        return driver.findElements(previousPageButton).stream()
                .anyMatch(button -> button.isDisplayed() && button.isEnabled());
    }

    private void clickWhenReady(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollIntoView(element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}
