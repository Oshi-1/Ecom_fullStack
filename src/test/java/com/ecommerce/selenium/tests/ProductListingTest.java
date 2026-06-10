package com.ecommerce.selenium.tests;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.ProductListingPage;
import com.ecommerce.selenium.utils.LoginTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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
        log("===== Product Listing complete execution successfully finished =====");
    }

    private boolean noBackendProductIsMissing(ProductListingPage productsPage) throws Exception {
        ProductResponse[] expectedProducts = fetchActiveProducts();
        Set<String> expectedNames = Arrays.stream(expectedProducts)
                .map(ProductResponse::getName)
                .collect(Collectors.toSet());

        return productsPage.totalProductCount() == expectedProducts.length
                && productsPage.displayedProductNames().equals(expectedNames);
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

    private void verify(boolean condition, String successMessage) {
        Assert.assertTrue(condition, successMessage + " - FAILED");
        log(successMessage);
    }

    private void log(String message) {
        System.out.println(message);
    }
}
