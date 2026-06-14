package com.ecommerce.selenium.tests;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.ProductDetailsPage;
import com.ecommerce.selenium.pages.ProductListingPage;
import com.ecommerce.selenium.utils.LoginTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.Assert;
import org.testng.annotations.Test;

public class US016ValidateProductDetailsTest extends BaseTest {

    @Test(priority = 1)
    public void T084_testSearchFunctionality() throws Exception {
        log("===== T084 Product Search execution started =====");
        ProductListingPage productsPage = openProductsAsNewUser();
        ProductResponse[] backendProducts = fetchActiveProducts();

        String validKeyword = "Headphones";
        Set<String> expectedSearchResults = expectedNamesForKeyword(backendProducts, validKeyword);
        productsPage.searchFor(validKeyword).waitUntilProductNamesAre(expectedSearchResults);

        verify(!expectedSearchResults.isEmpty(), "Valid search keyword returned expected backend products");
        verify(productsPage.allProductsContainKeyword(validKeyword), "Relevant product results displayed for valid keyword");
        verify(productsPage.displayedProductNames().equals(expectedSearchResults), "Actual search results matched expected results");

        String invalidKeyword = "no-product-" + Instant.now().toEpochMilli();
        productsPage.searchFor(invalidKeyword);
        verify(productsPage.totalProductCount() == 0, "Invalid keyword returned zero product cards");
        verify(productsPage.isEmptyMessageDisplayed(), "No Products Found validation passed");
        log("===== T084 Product Search execution passed =====");
    }

    @Test(priority = 2)
    public void T085_validateFiltering() throws Exception {
        log("===== T085 Product Filtering execution started =====");
        ProductListingPage productsPage = openProductsAsNewUser();

        String category = "Electronics";
        productsPage.filterByCategory(category);
        verify(productsPage.allProductsAreInCategory(category), "Category filter updated product list correctly");

        BigDecimal minPrice = new BigDecimal("1000");
        BigDecimal maxPrice = new BigDecimal("2500");
        productsPage.resetFilters().filterByPriceRange(minPrice, maxPrice);
        verify(productsPage.allProductsAreWithinPriceRange(minPrice, maxPrice),
                "Price filter updated product list within selected range");

        String brand = "SoundCore";
        productsPage.resetFilters().filterByBrand(brand);
        verify(productsPage.allProductsAreInBrand(brand), "Brand filter updated product list correctly");

        productsPage.resetFilters();
        verify(productsPage.searchKeywordValue().isEmpty()
                        && productsPage.selectedCategory().isEmpty()
                        && productsPage.selectedBrand().isEmpty()
                        && productsPage.minPriceValue().isEmpty()
                        && productsPage.maxPriceValue().isEmpty(),
                "All filter controls reset correctly");
        log("===== T085 Product Filtering execution passed =====");
    }

    @Test(priority = 3)
    public void T086_handleDynamicElements() throws Exception {
        log("===== T086 Dynamic Elements execution started =====");
        ProductListingPage productsPage = openProductsAsNewUser();

        verify(productsPage.isLoaded(), "Explicit waits confirmed page controls and AJAX-loaded content");
        verify(productsPage.allListedProductsVisible(), "AJAX-loaded product cards are visible");

        productsPage.searchFor("no-product-" + Instant.now().toEpochMilli());
        verify(productsPage.isEmptyMessageDisplayed(), "AJAX empty state displayed without stale element failure");

        productsPage.resetFilters();
        verify(productsPage.hasPagination(), "Pagination controls displayed for multi-page product listing");
        Set<String> firstPageNames = productsPage.displayedProductNames();
        productsPage.goToNextPage();
        verify(!productsPage.displayedProductNames().equals(firstPageNames), "Pagination next page updated product list");
        verify(productsPage.pageIndicatorText().contains("Page 2"), "Pagination indicator updated to page 2");
        productsPage.goToPreviousPage();
        verify(productsPage.displayedProductNames().equals(firstPageNames), "Pagination previous page restored first product list");
        log("===== T086 Dynamic Elements execution passed =====");
    }

    @Test(priority = 4)
    public void T087_implementReusableComponents() throws Exception {
        log("===== T087 Reusable Components execution started =====");
        ProductListingPage productsPage = openProductsAsNewUser();

        verify(productsPage.availableCategories().contains("Electronics"), "Reusable category reader method working");
        verify(productsPage.availableBrands().contains("SoundCore"), "Reusable brand reader method working");
        verify(productsPage.scrollThroughAndValidateAllProducts().isEmpty(),
                "Reusable product validation method checked name, image, and price");

        String selectedProductName = productsPage.firstProductName();
        String selectedProductPrice = productsPage.firstProductPrice();
        productsPage.openFirstProductDetails();

        ProductDetailsPage detailsPage = new ProductDetailsPage(driver, wait).waitUntilLoaded();
        verify(detailsPage.isOpen(), "Reusable details page wait opened selected product details");
        verify(detailsPage.displayedProductName().equals(selectedProductName), "Details product name matched listing");
        verify(priceValue(detailsPage.displayedProductPrice()).compareTo(priceValue(selectedProductPrice)) == 0,
                "Details product price matched listing");
        log("===== T087 Reusable Components execution passed =====");
    }

    private ProductListingPage openProductsAsNewUser() {
        startBackendIfNeeded();
        String email = "selenium.us016." + Instant.now().toEpochMilli() + "@example.com";
        new LoginTestUtils(driver, wait, frontendBaseUrl).registerAndOpenDashboard(email);
        log("Single sign-in completed for " + email);
        return new ProductListingPage(driver, wait, frontendBaseUrl).open();
    }

    private ProductResponse[] fetchActiveProducts() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(backendApiUrl + "/products")
                .toURL()
                .openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        Assert.assertEquals(connection.getResponseCode(), 200, "Product API should return HTTP 200.");

        try (InputStream response = connection.getInputStream()) {
            return new ObjectMapper().findAndRegisterModules().readValue(response, ProductResponse[].class);
        } finally {
            connection.disconnect();
        }
    }

    private Set<String> expectedNamesForKeyword(ProductResponse[] products, String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        return Arrays.stream(products)
                .filter(product -> product.getName().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .map(ProductResponse::getName)
                .collect(Collectors.toSet());
    }

    private BigDecimal priceValue(String priceText) {
        return new BigDecimal(priceText.trim()).stripTrailingZeros();
    }

    private void verify(boolean condition, String successMessage) {
        Assert.assertTrue(condition, successMessage + " - FAILED");
        log(successMessage);
    }

    private void log(String message) {
        System.out.println(message);
    }
}
