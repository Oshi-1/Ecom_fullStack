package com.ecommerce.selenium.tests;

import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.CartPage;
import com.ecommerce.selenium.pages.CheckoutPage;
import com.ecommerce.selenium.pages.ProductDetailsPage;
import com.ecommerce.selenium.pages.ProductListingPage;
import com.ecommerce.selenium.utils.LoginTestUtils;
import java.math.BigDecimal;
import java.time.Instant;
import org.testng.Assert;
import org.testng.annotations.Test;

public class US019ValidateCheckoutFlowTest extends BaseTest {

    @Test(priority = 1)
    public void T096_automateCheckoutInitiation() {
        log("===== T096 Checkout initiation automation started =====");
        CartPage cartPage = openCartWithProductAsNewUser(1);
        String productName = cartPageProductName();
        BigDecimal cartTotal = cartPage.totalAmount();

        CheckoutPage checkoutPage = cartPage.checkout();

        verify(checkoutPage.isOpen(), "Checkout page opened from cart");
        verify(checkoutPage.hasOrderSummaryForProduct(productName), "Checkout summary displayed selected cart product");
        verify(checkoutPage.totalItems() == 1, "Checkout summary item count matched cart quantity");
        verify(checkoutPage.totalAmount().compareTo(cartTotal) == 0, "Checkout summary total matched cart total");
        verify(checkoutPage.addressFieldsDisplayed(), "Checkout form address and contact inputs displayed");
        verify(checkoutPage.isCashOnDeliverySelected(), "Cash on Delivery selected by default");
        log("===== T096 Checkout initiation automation passed =====");
    }

    @Test(priority = 2)
    public void T097_validateFormInputs() {
        log("===== T097 Checkout form input validation started =====");
        CheckoutPage checkoutPage = openCartWithProductAsNewUser(1).checkout();

        verify(!checkoutPage.isPlaceOrderEnabled(), "Place Order button disabled when required inputs are empty");

        checkoutPage.enterHouseNo("221B")
                .enterStreet("Baker Street")
                .enterCity("Bengaluru")
                .enterPincode("12345")
                .enterState("Karnataka")
                .enterContactNumber("9876543210");

        verify(checkoutPage.isPlaceOrderEnabled(), "Place Order button enabled after required fields are filled");
        verify(!checkoutPage.pincodeInputValid(), "Five digit pincode rejected by checkout form");

        checkoutPage.submitExpectingValidationBlock();
        verify(checkoutPage.isOpen(), "Invalid pincode blocked order submission");

        checkoutPage.enterPincode("560001")
                .enterContactNumber("abc123");
        verify(checkoutPage.pincodeInputValid(), "Six digit pincode accepted by checkout form");

        checkoutPage.submitExpectingValidationBlock();
        verify(checkoutPage.isOpen(), "Invalid contact number blocked order submission");
        verify(checkoutPage.contactNumberErrorDisplayed(), "Invalid contact number rejected by checkout form");

        checkoutPage.enterContactNumber("+91 9876543210")
                .selectPaymentMethod("UPI")
                .submitOrderSuccessfully();

        verify(checkoutPage.orderSuccessDisplayed(), "Valid checkout form placed order successfully");
        verify(checkoutPage.successAddressContains("560001"), "Order success page displayed submitted delivery address");
        log("===== T097 Checkout form input validation passed =====");
    }

    private CartPage openCartWithProductAsNewUser(int quantity) {
        startBackendIfNeeded();
        String email = "selenium.us019." + Instant.now().toEpochMilli() + "@example.com";
        new LoginTestUtils(driver, wait, frontendBaseUrl).registerAndOpenDashboard(email);
        log("Single sign-in completed for " + email);

        ProductListingPage productsPage = new ProductListingPage(driver, wait, frontendBaseUrl).open();
        productsPage.openFirstProductDetails();
        ProductDetailsPage detailsPage = new ProductDetailsPage(driver, wait).waitUntilLoaded();
        currentProductName = detailsPage.displayedProductName();

        detailsPage.setQuantity(quantity).addToCart();
        return detailsPage.viewCart();
    }

    private String currentProductName;

    private String cartPageProductName() {
        return currentProductName;
    }

    private void verify(boolean condition, String successMessage) {
        Assert.assertTrue(condition, successMessage + " - FAILED");
        log(successMessage);
    }

    private void log(String message) {
        System.out.println(message);
    }
}
