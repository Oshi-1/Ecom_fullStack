package com.ecommerce.selenium.tests;

import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.CartPage;
import com.ecommerce.selenium.pages.ProductDetailsPage;
import com.ecommerce.selenium.pages.ProductListingPage;
import com.ecommerce.selenium.utils.LoginTestUtils;
import java.math.BigDecimal;
import java.time.Instant;
import org.testng.Assert;
import org.testng.annotations.Test;

public class US017ValidateAddToCartTest extends BaseTest {

    @Test(priority = 1)
    public void T088_automateAddToCartFunctionality() {
        log("===== T088 Add to Cart automation started =====");
        ProductDetailsPage detailsPage = openFirstProductDetailsAsNewUser();

        String selectedProductName = detailsPage.displayedProductName();
        detailsPage.setQuantity(1).addToCart();

        verify(detailsPage.successMessageDisplayed(), "Add to Cart success message displayed");

        CartPage cartPage = detailsPage.viewCart();
        verify(cartPage.isOpen(), "Cart page opened after selecting View Cart");
        verify(cartPage.hasProduct(selectedProductName), "Added product is visible in cart");
        log("===== T088 Add to Cart automation passed =====");
    }

    @Test(priority = 2)
    public void T089_validateProductAddition() {
        log("===== T089 Validate product addition started =====");
        ProductDetailsPage detailsPage = openFirstProductDetailsAsNewUser();

        String selectedProductName = detailsPage.displayedProductName();
        BigDecimal selectedProductPrice = priceValue(detailsPage.displayedProductPrice());
        int quantity = 2;

        detailsPage.setQuantity(quantity).addToCart();
        CartPage cartPage = detailsPage.viewCart();

        verify(cartPage.hasProduct(selectedProductName), "Correct product name added to cart");
        verify(cartPage.quantityForProduct(selectedProductName) == quantity, "Correct product quantity added to cart");
        verify(cartPage.priceForProduct(selectedProductName).compareTo(selectedProductPrice) == 0,
                "Cart item price matched selected product price");
        verify(cartPage.subtotalForProduct(selectedProductName).compareTo(selectedProductPrice.multiply(BigDecimal.valueOf(quantity))) == 0,
                "Cart item subtotal matched price multiplied by quantity");
        verify(cartPage.totalItems() == quantity, "Cart summary item count matched added quantity");
        verify(cartPage.totalAmount().compareTo(selectedProductPrice.multiply(BigDecimal.valueOf(quantity))) == 0,
                "Cart summary total matched added product subtotal");
        log("===== T089 Validate product addition passed =====");
    }

    private ProductDetailsPage openFirstProductDetailsAsNewUser() {
        startBackendIfNeeded();
        String email = "selenium.us017." + Instant.now().toEpochMilli() + "@example.com";
        new LoginTestUtils(driver, wait, frontendBaseUrl).registerAndOpenDashboard(email);
        log("Single sign-in completed for " + email);

        ProductListingPage productsPage = new ProductListingPage(driver, wait, frontendBaseUrl).open();
        productsPage.openFirstProductDetails();
        return new ProductDetailsPage(driver, wait).waitUntilLoaded();
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
