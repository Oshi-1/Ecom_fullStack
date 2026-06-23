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

        verify(checkoutPage.isPlaceOrderEnabled(), "Place Order button available so checkout validation can show popup");

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

    @Test(priority = 3)
    public void T098_validateInvalidCheckoutInputsShowValidationPopup() {
        log("===== T098 Invalid checkout input popup validation started =====");
        CheckoutPage checkoutPage = openCartWithProductAsNewUser(1).checkout();

        checkoutPage.enterValidCheckoutDetails()
                .enterContactNumber("abc123");

        verify(checkoutPage.isPlaceOrderEnabled(), "Place Order button enabled when all fields have values");

        checkoutPage.submitExpectingValidationBlock();

        verify(checkoutPage.isOpen(), "Invalid checkout input kept user on checkout page");
        verify(checkoutPage.validationPopupDisplayed("Enter a valid contact number"),
                "Invalid contact number popup displayed");
        verify(checkoutPage.contactNumberErrorDisplayed(),
                "Invalid contact number field error displayed");
        verify(!checkoutPage.orderSuccessDisplayed(), "Order was not placed for invalid checkout input");
        log("===== T098 Invalid checkout input popup validation passed =====");
    }

    @Test(priority = 4)
    public void T099_validateCheckoutBlockedWhenAnyFieldIsEmpty() {
        log("===== T099 Empty checkout field validation started =====");
        CheckoutPage checkoutPage = openCartWithProductAsNewUser(1).checkout();

        String[][] requiredFields = {
                {"houseNo", "221B", "House / Flat No."},
                {"street", "Baker Street", "Street / Area"},
                {"city", "Bengaluru", "City"},
                {"pincode", "560001", "Pincode"},
                {"state", "Karnataka", "State"},
                {"contactNumber", "9876543210", "Contact Number"}
        };

        checkoutPage.enterValidCheckoutDetails();
        verify(checkoutPage.isPlaceOrderEnabled(), "Place Order button enabled after all checkout fields are filled");

        for (String[] field : requiredFields) {
            checkoutPage.clearField(field[0]);

            verify(checkoutPage.isPlaceOrderEnabled(),
                    "Place Order button remains clickable when " + field[2] + " is empty");

            checkoutPage.submitExpectingValidationBlock();

            verify(checkoutPage.validationPopupDisplayed("Required fields cannot be left blank"),
                    "Required fields popup displayed when " + field[2] + " is empty");
            verify(checkoutPage.isOpen(), "Checkout page remained open after trying checkout with empty " + field[2]);
            verify(!checkoutPage.orderSuccessDisplayed(), "Order was not placed when " + field[2] + " is empty");

            checkoutPage.clearField(field[0]);
            switch (field[0]) {
                case "houseNo" -> checkoutPage.enterHouseNo(field[1]);
                case "street" -> checkoutPage.enterStreet(field[1]);
                case "city" -> checkoutPage.enterCity(field[1]);
                case "pincode" -> checkoutPage.enterPincode(field[1]);
                case "state" -> checkoutPage.enterState(field[1]);
                case "contactNumber" -> checkoutPage.enterContactNumber(field[1]);
                default -> throw new IllegalArgumentException("Unsupported checkout field: " + field[0]);
            }
            verify(checkoutPage.isPlaceOrderEnabled(), "Place Order button re-enabled after refilling " + field[2]);
        }

        log("===== T099 Empty checkout field validation passed =====");
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
