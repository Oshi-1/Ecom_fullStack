package com.ecommerce.selenium.tests;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.ProductDetailsPage;
import com.ecommerce.selenium.pages.ProductListingPage;
import com.ecommerce.selenium.utils.LoginTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProductListingTest extends BaseTest {

    @Test
    public void validateCompleteProductListingAfterSingleSignIn() throws Exception {
        log("===== Product Listing complete execution started =====");
        startBackendIfNeeded();

        String email = "selenium.products." + Instant.now().toEpochMilli() + "@example.com";
        new LoginTestUtils(driver, wait, frontendBaseUrl).registerAndOpenDashboard(email);
        log("Single sign-in successfully complete ho gaya");

        ProductListingPage productsPage = new ProductListingPage(driver, wait, frontendBaseUrl).open();
        log("Product Listing Page");

        verify(productsPage.isProductListingPageOpen(), "Product Listing Page open ho gayi");
        verify(productsPage.isLoaded(), "Page successfully load ho gayi");
        verify(productsPage.isProductListingSectionDisplayed(), "Product listing section display ho raha hai");
        verify(productsPage.totalProductCount() > 0, "Products page par load ho gaye hain");
        verify(productsPage.totalProductCount() > 0,
                "Product count 0 se greater hai (kam se kam 1 product present hai)");

        log("Har product ko dekhne aur validate karne ke liye page niche scroll ho raha hai");
        List<String> scrollValidationErrors = productsPage.scrollThroughAndValidateAllProducts();
        verify(scrollValidationErrors.isEmpty(),
                "Browser ne " + productsPage.totalProductCount()
                        + " products tak scroll karke name, image aur price validate kiya. Errors: "
                        + scrollValidationErrors);

        verify(productsPage.allListedProductsVisible(), "Product Visibility Sabhi listed products visible hain");
        verify(productsPage.allProductNamesVisible(), "Har product ka Name visible hai");
        verify(productsPage.allProductImagesVisible(), "Har product ki Image visible hai");
        verify(productsPage.allProductPricesVisible(), "Har product ka Price visible hai");
        verify(productsPage.hiddenProductCount() == 0, "Koi hidden product nahi mila");
        verify(noBackendProductIsMissing(productsPage), "Koi missing product nahi mila");

        verify(productNamesAndPricesMatchBackend(productsPage),
                "T082: Product listing par product name aur price backend data se match kar rahe hain");

        ProductResponse[] backendProducts = fetchActiveProducts();
        String searchKeyword = "Headphones";
        Set<String> expectedSearchResults = expectedNamesForKeyword(backendProducts, searchKeyword);
        productsPage.searchFor(searchKeyword).waitUntilProductNamesAre(expectedSearchResults);
        verify(!expectedSearchResults.isEmpty(), "T084: Search test ke liye expected products backend se mile");
        verify(productsPage.allProductsContainKeyword(searchKeyword),
                "T084: Search functionality keyword '" + searchKeyword + "' ke liye sahi products dikha rahi hai");
        verify(productsPage.displayedProductNames().equals(expectedSearchResults),
                "T084: Search results backend search expectation se match kar rahe hain");

        productsPage.resetFilters();
        verify(productsPage.allProductNamesAcrossPages().size() == backendProducts.length,
                "T084: Reset ke baad complete product list wapas aa gayi");

        String category = "Electronics";
        Set<String> expectedCategoryResults = expectedNamesForCategory(backendProducts, category);
        productsPage.filterByCategory(category).waitUntilProductNamesAre(expectedCategoryResults);
        verify(!expectedCategoryResults.isEmpty(), "T085: Filter test ke liye expected category products backend se mile");
        verify(productsPage.allProductsAreInCategory(category),
                "T085: Category filtering '" + category + "' ke liye sirf matching products dikha rahi hai");
        verify(productsPage.displayedProductNames().equals(expectedCategoryResults),
                "T085: Filtered products backend category expectation se match kar rahe hain");

        String impossibleKeyword = "no-product-" + Instant.now().toEpochMilli();
        productsPage.searchFor(impossibleKeyword);
        verify(productsPage.totalProductCount() == 0 && productsPage.isEmptyMessageDisplayed(),
                "T086: Dynamic empty state handle ho raha hai jab search result nahi milta");
        productsPage.resetFilters();
        verify(productsPage.allProductNamesAcrossPages().equals(expectedNamesForAll(backendProducts)),
                "T086: Dynamic reset ke baad complete product list wapas aa gayi");
        verify(productsPage.searchKeywordValue().isEmpty() && productsPage.selectedCategory().isEmpty(),
                "T086: Dynamic reset ke baad search aur filter controls clean ho gaye");
        verify(productsPage.allListedProductsVisible(),
                "T086: Dynamic reload ke baad product cards stale/hide nahi huye");
        verify(productsPage.availableCategories().contains(category),
                "T087: Reusable ProductListingPage component category options read kar raha hai");

        String selectedProductName = productsPage.firstProductName();
        String selectedProductPrice = productsPage.firstProductPrice();
        productsPage.openFirstProductDetails();

        ProductDetailsPage detailsPage = new ProductDetailsPage(driver, wait).waitUntilLoaded();
        verify(detailsPage.isOpen(), "T083: Product card par click karne ke baad product details page open ho gaya");
        verify(detailsPage.displayedProductName().equals(selectedProductName),
                "T083: Details page par selected product ka name sahi display ho raha hai");
        verify(priceValue(detailsPage.displayedProductPrice()).compareTo(priceValue(selectedProductPrice)) == 0,
                "T083: Details page par selected product ka price sahi display ho raha hai");

        log("===== Product Listing complete execution successfully finished =====");
    }

    private boolean noBackendProductIsMissing(ProductListingPage productsPage) throws Exception {
        ProductResponse[] expectedProducts = fetchActiveProducts();
        Set<String> expectedNames = Arrays.stream(expectedProducts)
                .map(ProductResponse::getName)
                .collect(Collectors.toSet());

        return productsPage.allProductNamesAcrossPages().size() == expectedProducts.length
                && productsPage.allProductNamesAcrossPages().equals(expectedNames);
    }

    private boolean productNamesAndPricesMatchBackend(ProductListingPage productsPage) throws Exception {
        Map<String, BigDecimal> expectedProductNamePrices = Arrays.stream(fetchActiveProducts())
                .collect(Collectors.toMap(
                        ProductResponse::getName,
                        product -> product.getPrice().stripTrailingZeros()));

        Map<String, BigDecimal> actualProductNamePrices = productsPage.allProductNamePricesAcrossPages()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> priceValue(entry.getValue())));

        return actualProductNamePrices.equals(expectedProductNamePrices);
    }

    private ProductResponse[] fetchActiveProducts() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(backendApiUrl + "/products")
                .toURL()
                .openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        Assert.assertEquals(connection.getResponseCode(), 200,
                "Backend product API should return HTTP 200 for missing product validation.");

        try (InputStream response = connection.getInputStream()) {
            return new ObjectMapper().findAndRegisterModules().readValue(response, ProductResponse[].class);
        } finally {
            connection.disconnect();
        }
    }

    private Set<String> expectedNamesForAll(ProductResponse[] products) {
        return Arrays.stream(products)
                .map(ProductResponse::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> expectedNamesForKeyword(ProductResponse[] products, String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        return Arrays.stream(products)
                .filter(product -> product.getName().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .map(ProductResponse::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> expectedNamesForCategory(ProductResponse[] products, String category) {
        return Arrays.stream(products)
                .filter(product -> product.getCategory().equalsIgnoreCase(category))
                .map(ProductResponse::getName)
                .collect(Collectors.toSet());
    }

    private void verify(boolean condition, String successMessage) {
        Assert.assertTrue(condition, successMessage + " - FAILED");
        log(successMessage);
    }

    private BigDecimal priceValue(String priceText) {
        return new BigDecimal(priceText.trim()).stripTrailingZeros();
    }

    private void log(String message) {
        System.out.println(message);
    }
}
