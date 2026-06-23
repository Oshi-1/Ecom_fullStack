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

        cartPage.increaseQuantityForProduct(selectedProductName);
        int incrementedQuantity = quantity + 1;
        verify(cartPage.quantityForProduct(selectedProductName) == incrementedQuantity,
                "Cart plus button increased product quantity");
        verify(cartPage.subtotalForProduct(selectedProductName)
                        .compareTo(selectedProductPrice.multiply(BigDecimal.valueOf(incrementedQuantity))) == 0,
                "Cart subtotal updated after plus button");
        verify(cartPage.totalItems() == incrementedQuantity, "Cart summary item count updated after plus button");
        verify(cartPage.totalAmount().compareTo(selectedProductPrice.multiply(BigDecimal.valueOf(incrementedQuantity))) == 0,
                "Cart summary total updated after plus button");
        verify(!cartPage.quantityUpdateErrorDisplayed(), "No quantity update error displayed after plus button");

        cartPage.decreaseQuantityForProduct(selectedProductName);
        verify(cartPage.quantityForProduct(selectedProductName) == quantity,
                "Cart minus button decreased product quantity");
        verify(cartPage.subtotalForProduct(selectedProductName)
                        .compareTo(selectedProductPrice.multiply(BigDecimal.valueOf(quantity))) == 0,
                "Cart subtotal updated after minus button");
        verify(cartPage.totalItems() == quantity, "Cart summary item count updated after minus button");
        verify(cartPage.totalAmount().compareTo(selectedProductPrice.multiply(BigDecimal.valueOf(quantity))) == 0,
                "Cart summary total updated after minus button");
        verify(!cartPage.quantityUpdateErrorDisplayed(), "No quantity update error displayed after minus button");
        log("===== T089 Validate product addition passed =====");
    }

    @Test(priority = 3)
    public void T090_validateCartPlusMinusAutoQuantityAndPriceUpdate() {
        log("===== T090 Cart plus/minus quantity and price sync started =====");
        ProductDetailsPage detailsPage = openFirstProductDetailsAsNewUser();

        String selectedProductName = detailsPage.displayedProductName();
        BigDecimal selectedProductPrice = priceValue(detailsPage.displayedProductPrice());
        int initialQuantity = 3;

        detailsPage.setQuantity(initialQuantity).addToCart();
        CartPage cartPage = detailsPage.viewCart();

        verifyCartTotals(cartPage, selectedProductName, selectedProductPrice, initialQuantity,
                "Initial cart quantity and price matched");

        int incrementedQuantity = initialQuantity + 1;
        cartPage.increaseQuantityForProduct(selectedProductName)
                .waitForSubtotal(selectedProductName, selectedProductPrice.multiply(BigDecimal.valueOf(incrementedQuantity)))
                .waitForSummary(incrementedQuantity, selectedProductPrice.multiply(BigDecimal.valueOf(incrementedQuantity)));
        verifyCartTotals(cartPage, selectedProductName, selectedProductPrice, incrementedQuantity,
                "Plus button auto incremented quantity and price");
        verify(!cartPage.quantityUpdateErrorDisplayed(), "No error displayed after plus button automation");

        int decrementedQuantity = initialQuantity;
        cartPage.decreaseQuantityForProduct(selectedProductName)
                .waitForSubtotal(selectedProductName, selectedProductPrice.multiply(BigDecimal.valueOf(decrementedQuantity)))
                .waitForSummary(decrementedQuantity, selectedProductPrice.multiply(BigDecimal.valueOf(decrementedQuantity)));
        verifyCartTotals(cartPage, selectedProductName, selectedProductPrice, decrementedQuantity,
                "Minus button auto decremented quantity and price");
        verify(!cartPage.quantityUpdateErrorDisplayed(), "No error displayed after minus button automation");

        log("===== T090 Cart plus/minus quantity and price sync passed =====");
    }

    @Test(priority = 4)
    public void T091_validateCartMinusButtonDisabledAtMinimumQuantity() {
        log("===== T091 Cart minus button minimum quantity validation started =====");
        ProductDetailsPage detailsPage = openFirstProductDetailsAsNewUser();

        String selectedProductName = detailsPage.displayedProductName();
        BigDecimal selectedProductPrice = priceValue(detailsPage.displayedProductPrice());

        detailsPage.setQuantity(1).addToCart();
        CartPage cartPage = detailsPage.viewCart();

        verifyCartTotals(cartPage, selectedProductName, selectedProductPrice, 1,
                "Cart opened with minimum quantity one");
        verify(cartPage.isDecreaseDisabledForProduct(selectedProductName),
                "Minus button is disabled when product quantity is one");
        verify(!cartPage.quantityUpdateErrorDisplayed(), "No quantity update error displayed at minimum quantity");
        log("===== T091 Cart minus button minimum quantity validation passed =====");
    }

    @Test(priority = 5)
    public void T092_automateQuantityIncrementAndDecrementUsingPlusMinusButtons() {
        log("===== T092 Automated plus/minus quantity button demo started =====");
        ProductDetailsPage detailsPage = openFirstProductDetailsAsNewUser();

        String selectedProductName = detailsPage.displayedProductName();
        BigDecimal selectedProductPrice = priceValue(detailsPage.displayedProductPrice());

        detailsPage.setQuantity(1).addToCart();
        CartPage cartPage = detailsPage.viewCart();

        verifyCartTotals(cartPage, selectedProductName, selectedProductPrice, 1,
                "Automation opened cart with quantity one");

        cartPage.increaseQuantityForProduct(selectedProductName);
        verifyCartTotals(cartPage, selectedProductName, selectedProductPrice, 2,
                "Selenium clicked plus button first time and quantity became two");

        cartPage.increaseQuantityForProduct(selectedProductName);
        verifyCartTotals(cartPage, selectedProductName, selectedProductPrice, 3,
                "Selenium clicked plus button second time and quantity became three");

        cartPage.decreaseQuantityForProduct(selectedProductName);
        verifyCartTotals(cartPage, selectedProductName, selectedProductPrice, 2,
                "Selenium clicked minus button and quantity became two");

        verify(!cartPage.quantityUpdateErrorDisplayed(), "No quantity update error displayed during plus/minus demo");
        log("===== T092 Automated plus/minus quantity button demo passed =====");
    }

    @Test(priority = 6)
    public void T093_automateRemoveProductFromCartUsingRemoveButton() {
        log("===== T093 Automated cart remove button validation started =====");
        ProductDetailsPage detailsPage = openFirstProductDetailsAsNewUser();

        String selectedProductName = detailsPage.displayedProductName();
        BigDecimal selectedProductPrice = priceValue(detailsPage.displayedProductPrice());

        detailsPage.setQuantity(1).addToCart();
        CartPage cartPage = detailsPage.viewCart();

        verifyCartTotals(cartPage, selectedProductName, selectedProductPrice, 1,
                "Automation opened cart with one product before remove");

        cartPage.removeProduct(selectedProductName);

        verify(!cartPage.hasProduct(selectedProductName), "Removed product is no longer visible in cart");
        verify(cartPage.isEmpty(), "Cart empty state displayed after removing the only product");
        verify(cartPage.itemRemoveSuccessDisplayed(), "Item removed success message displayed");
        verify(!cartPage.itemRemoveErrorDisplayed(), "No remove item error displayed");
        log("===== T093 Automated cart remove button validation passed =====");
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

    private void verifyCartTotals(CartPage cartPage, String productName, BigDecimal unitPrice, int quantity,
            String message) {
        BigDecimal expectedAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        verify(cartPage.quantityForProduct(productName) == quantity, message + " - quantity");
        verify(cartPage.subtotalForProduct(productName).compareTo(expectedAmount) == 0,
                message + " - line subtotal");
        verify(cartPage.totalItems() == quantity, message + " - summary items");
        verify(cartPage.totalAmount().compareTo(expectedAmount) == 0, message + " - summary total");
    }

    private void verify(boolean condition, String successMessage) {
        Assert.assertTrue(condition, successMessage + " - FAILED");
        log(successMessage);
    }

    private void log(String message) {
        System.out.println(message);
    }
}
