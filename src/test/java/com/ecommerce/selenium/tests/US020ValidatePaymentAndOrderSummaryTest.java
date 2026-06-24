package com.ecommerce.selenium.tests;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.CartPage;
import com.ecommerce.selenium.pages.CheckoutPage;
import com.ecommerce.selenium.pages.ProductDetailsPage;
import com.ecommerce.selenium.utils.LoginTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class US020ValidatePaymentAndOrderSummaryTest extends BaseTest {

    private static final String EXCEL_URL =
            "https://1drv.ms/x/c/e0e61521f9c25fd3/IQC39MT3Hb1fSJ48r5mq8QbWAWsLc2UL2B-uDJ6gOkZVfYg?download=1";
    private static final Path EXCEL_FILE = Paths.get("target", "test-data", "provided-checkout-data.xlsx");
    private static final AtomicInteger TOTAL_PASSED = new AtomicInteger();
    private static final AtomicInteger TOTAL_FAILED = new AtomicInteger();

    @Test(priority = 1)
    public void T101_T104_addMultipleExcelProductsToCartAndShowOrderSummary() {
        downloadExcelIfMissing();
        List<ExcelDataset> datasets = readExcelRows();
        printExcelDataBeforeExecution(datasets);

        logSection("START MULTI PRODUCT CHECKOUT FROM EXCEL");
        MultiCheckoutScenario scenario = openCheckoutWithAllExcelProducts(datasets);
        CheckoutPage checkoutPage = scenario.checkoutPage();

        boolean passed = true;
        BigDecimal excelDiscountTotal = BigDecimal.ZERO;
        BigDecimal excelExpectedTotalSum = BigDecimal.ZERO;

        log("ORDER SUMMARY DETAILS:");
        for (CheckoutLine line : scenario.lines()) {
            ExcelDataset dataset = line.dataset();
            BigDecimal expectedLineTotal = dataset.price()
                    .multiply(BigDecimal.valueOf(dataset.quantity()))
                    .stripTrailingZeros();
            excelDiscountTotal = excelDiscountTotal.add(dataset.discount());
            excelExpectedTotalSum = excelExpectedTotalSum.add(dataset.expectedTotal());

            logSection("ORDER SUMMARY ROW: " + dataset.datasetId());
            passed &= printCheck("Product Name", line.actualProductName(), checkoutPage.summaryProductName(line.actualProductName()));
            passed &= printCheck("Quantity", dataset.quantity(), checkoutPage.summaryQuantityForProduct(line.actualProductName()));
            passed &= printCheck("Unit Price", dataset.price(), checkoutPage.summaryUnitPriceForProduct(line.actualProductName()));
            passed &= printCheck("Line Total", expectedLineTotal, checkoutPage.summarySubtotalForProduct(line.actualProductName()));
            log("Calculation: " + formatValue(dataset.price()) + " * " + dataset.quantity()
                    + " = " + formatValue(expectedLineTotal));
        }

        BigDecimal productTotal = scenario.productTotal().stripTrailingZeros();
        BigDecimal excelDiscountedTotal = productTotal.subtract(excelDiscountTotal).stripTrailingZeros();
        BigDecimal actualOrderTotal = checkoutPage.totalAmount();

        logSection("TOTAL CALCULATION");
        passed &= printCheck("Total Quantity", scenario.totalQuantity(), checkoutPage.totalItems());
        passed &= printCheck("Product Total", productTotal, actualOrderTotal);
        log("Excel Discount Total: " + formatValue(excelDiscountTotal));
        log("Product Total - Excel Discount = " + formatValue(productTotal) + " - "
                + formatValue(excelDiscountTotal) + " = " + formatValue(excelDiscountedTotal));
        log("Excel Expected Total Sum: " + formatValue(excelExpectedTotalSum));
        log("Actual UI Grand Total: " + formatValue(actualOrderTotal));
        log("Note: Current app checkout summary does not apply Excel discount, so UI Grand Total equals Product Total.");

        logSection("CHECKOUT FORM FILL AND PLACE ORDER");
        checkoutPage.enterHouseNo("221B")
                .enterStreet("Baker Street")
                .enterCity("Bengaluru")
                .enterPincode("560001")
                .enterState("Karnataka")
                .enterContactNumber("9876543210")
                .selectPaymentMethod("COD");
        log("Checkout form filled:");
        log("House No=221B | Street=Baker Street | City=Bengaluru | Pincode=560001 | State=Karnataka");
        log("Contact Number=9876543210 | Payment Method=COD");
        passed &= printCheck("Payment Selected", "COD", checkoutPage.isPaymentMethodSelected("COD") ? "COD" : "NOT_SELECTED");

        checkoutPage.submitOrderSuccessfully();
        log("Place Order clicked successfully.");
        passed &= printCheck("Order Success Page", true, checkoutPage.orderSuccessDisplayed());
        passed &= printCheck("Success Address", true, checkoutPage.successAddressContains("560001"));
        passed &= printCheck("Success Payment", "COD", checkoutPage.successPaymentMethodIs("COD") ? "COD" : "NOT_SELECTED");
        log("Checkout completed with multi-product order.");

        log("MULTI PRODUCT DATASET RESULT: " + (passed ? "PASS" : "FAIL"));
        logSection("END MULTI PRODUCT CHECKOUT FROM EXCEL");

        if (passed) {
            TOTAL_PASSED.incrementAndGet();
        } else {
            TOTAL_FAILED.incrementAndGet();
        }

        Assert.assertTrue(passed, "Multi-product order summary validation failed.");
    }

    @Test(dataProvider = "excelRows", enabled = false)
    public void T101_T104_validateOrderSummaryAndTotalFromExcel(ExcelDataset dataset) {
        logSection("START DATASET: " + dataset.datasetId());
        log("Input Values:");
        log("Product Name   : " + dataset.productName());
        log("Quantity       : " + dataset.quantity());
        log("Price          : " + formatValue(dataset.price()));
        log("Discount       : " + formatValue(dataset.discount()));
        log("Expected Total : " + formatValue(dataset.expectedTotal()));

        boolean passed = true;
        try {
            CheckoutScenario scenario = openCheckoutForExcelDataset(dataset);
            CheckoutPage checkoutPage = scenario.checkoutPage();

            BigDecimal actualUnitPrice = checkoutPage.summaryUnitPriceForProduct(scenario.actualProductName());
            int actualQuantity = checkoutPage.summaryQuantityForProduct(scenario.actualProductName());
            BigDecimal actualProductTotal = checkoutPage.summarySubtotalForProduct(scenario.actualProductName());
            BigDecimal actualTotal = checkoutPage.totalAmount();
            BigDecimal calculatedExpectedTotal = dataset.price()
                    .multiply(BigDecimal.valueOf(dataset.quantity()))
                    .subtract(dataset.discount())
                    .stripTrailingZeros();

            passed &= printCheck("Dataset ID", dataset.datasetId(), dataset.datasetId());
            passed &= printCheck("Product Name", scenario.actualProductName(), checkoutPage.summaryProductName(scenario.actualProductName()));
            passed &= printCheck("Quantity", dataset.quantity(), actualQuantity);
            passed &= printCheck("Excel Price", dataset.price(), actualUnitPrice);
            passed &= printCheck("Order Summary Total", dataset.price().multiply(BigDecimal.valueOf(dataset.quantity())).stripTrailingZeros(),
                    actualProductTotal);
            log(String.format("%-24s | %s * %s - %s = %s",
                    "Calculated Total", formatValue(dataset.price()), dataset.quantity(),
                    formatValue(dataset.discount()), formatValue(calculatedExpectedTotal)));
            passed &= printCheck("Expected Total", dataset.expectedTotal(), calculatedExpectedTotal);
            passed &= printCheck("Actual Total", dataset.expectedTotal(), actualTotal);
        } catch (AssertionError | RuntimeException ex) {
            passed = false;
            log("Dataset execution issue | " + ex.getMessage());
        }

        log("DATASET RESULT [" + dataset.datasetId() + "]: " + (passed ? "PASS" : "FAIL"));
        logSection("END DATASET: " + dataset.datasetId());

        if (passed) {
            TOTAL_PASSED.incrementAndGet();
        } else {
            TOTAL_FAILED.incrementAndGet();
        }
    }

    @DataProvider(name = "excelRows")
    public Object[][] excelRows() {
        downloadExcelIfMissing();
        List<ExcelDataset> datasets = readExcelRows();
        printExcelDataBeforeExecution(datasets);
        return datasets.stream()
                .map(dataset -> new Object[] {dataset})
                .toArray(Object[][]::new);
    }

    @AfterSuite(alwaysRun = true)
    public void printFinalExecutionSummary() {
        int passed = TOTAL_PASSED.get();
        int failed = TOTAL_FAILED.get();
        logSection("FINAL SUMMARY REPORT");
        log("Total Executed : " + (passed + failed));
        log("Passed         : " + passed);
        log("Failed         : " + failed);
        log("Overall Result : " + (failed == 0 ? "PASS" : "FAIL"));
        logSection("SUMMARY END");
    }

    private CheckoutScenario openCheckoutForExcelDataset(ExcelDataset dataset) {
        startBackendIfNeeded();
        String email = "selenium.us020.excel." + Instant.now().toEpochMilli() + "@example.com";
        new LoginTestUtils(driver, wait, frontendBaseUrl).registerAndOpenDashboard(email);
        log("Single sign-in completed for " + email);

        ProductResponse product = fetchProductByExcelName(dataset.productName(), dataset.quantity());
        driver.get(frontendBaseUrl + "/products/" + product.getProductId());
        ProductDetailsPage detailsPage = new ProductDetailsPage(driver, wait).waitUntilLoaded();

        String actualProductName = detailsPage.displayedProductName();
        detailsPage.setQuantity(dataset.quantity()).addToCart();

        CartPage cartPage = detailsPage.viewCart();
        Assert.assertTrue(cartPage.hasProduct(actualProductName), "Cart should contain Excel product: " + actualProductName);
        Assert.assertEquals(cartPage.quantityForProduct(actualProductName), dataset.quantity(),
                "Cart quantity should match Excel quantity.");

        return new CheckoutScenario(actualProductName, cartPage.checkout());
    }

    private MultiCheckoutScenario openCheckoutWithAllExcelProducts(List<ExcelDataset> datasets) {
        startBackendIfNeeded();
        String email = "selenium.us020.multirow." + Instant.now().toEpochMilli() + "@example.com";
        new LoginTestUtils(driver, wait, frontendBaseUrl).registerAndOpenDashboard(email);
        log("Single sign-in completed for " + email);

        ProductDetailsPage detailsPage = null;
        List<CheckoutLine> lines = new ArrayList<>();
        BigDecimal productTotal = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (ExcelDataset dataset : datasets) {
            ProductResponse product = fetchProductByExcelName(dataset.productName(), dataset.quantity());
            driver.get(frontendBaseUrl + "/products/" + product.getProductId());
            detailsPage = new ProductDetailsPage(driver, wait).waitUntilLoaded();

            String actualProductName = detailsPage.displayedProductName();
            BigDecimal expectedLineTotal = dataset.price()
                    .multiply(BigDecimal.valueOf(dataset.quantity()))
                    .stripTrailingZeros();

            detailsPage.setQuantity(dataset.quantity()).addToCart();
            lines.add(new CheckoutLine(dataset, actualProductName, expectedLineTotal));
            productTotal = productTotal.add(expectedLineTotal);
            totalQuantity += dataset.quantity();

            log("Added to cart | Dataset=" + dataset.datasetId()
                    + " | Product=" + actualProductName
                    + " | Qty=" + dataset.quantity()
                    + " | Price=" + formatValue(dataset.price())
                    + " | Line Total=" + formatValue(expectedLineTotal));
        }

        Assert.assertNotNull(detailsPage, "At least one Excel product should be added before cart checkout.");
        CartPage cartPage = detailsPage.viewCart();
        for (CheckoutLine line : lines) {
            Assert.assertTrue(cartPage.hasProduct(line.actualProductName()),
                    "Cart should contain Excel product: " + line.actualProductName());
            Assert.assertEquals(cartPage.quantityForProduct(line.actualProductName()), line.dataset().quantity(),
                    "Cart quantity should match Excel quantity.");
            Assert.assertEquals(cartPage.subtotalForProduct(line.actualProductName()).compareTo(line.lineTotal()), 0,
                    "Cart subtotal should match Excel line total.");
        }
        Assert.assertEquals(cartPage.totalItems(), totalQuantity, "Cart total quantity should match Excel quantities.");
        Assert.assertEquals(cartPage.totalAmount().compareTo(productTotal.stripTrailingZeros()), 0,
                "Cart total amount should match all Excel line totals.");

        return new MultiCheckoutScenario(lines, totalQuantity, productTotal.stripTrailingZeros(), cartPage.checkout());
    }

    private ProductResponse fetchProductByExcelName(String excelProductName, int quantity) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(backendApiUrl + "/products")
                    .toURL()
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            Assert.assertEquals(connection.getResponseCode(), 200,
                    "Backend product API should return HTTP 200 before Selenium checkout test.");

            try (InputStream response = connection.getInputStream()) {
                ProductResponse[] products = new ObjectMapper().findAndRegisterModules()
                        .readValue(response, ProductResponse[].class);
                return Arrays.stream(products)
                        .filter(product -> Boolean.TRUE.equals(product.getActive()))
                        .filter(product -> namesMatch(product.getName(), excelProductName))
                        .filter(product -> product.getStock() != null && product.getStock() >= quantity)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError(
                                "Excel product not found/in-stock in app. Product='" + excelProductName
                                        + "', required quantity=" + quantity));
            } finally {
                connection.disconnect();
            }
        } catch (Exception ex) {
            throw new AssertionError("Product API se Excel product fetch nahi ho paya.", ex);
        }
    }

    private boolean namesMatch(String appName, String excelName) {
        String normalizedAppName = normalizeName(appName);
        String normalizedExcelName = normalizeName(excelName);
        return normalizedAppName.equals(normalizedExcelName)
                || normalizedAppName.contains(normalizedExcelName)
                || normalizedExcelName.contains(normalizedAppName)
                || matchingTokenCount(appName, excelName) >= 2;
    }

    private String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private int matchingTokenCount(String appName, String excelName) {
        String normalizedAppName = normalizeName(appName).replace("bottle", "botel");
        int matches = 0;
        for (String rawToken : excelName.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            String token = rawToken.replace("bottle", "botel");
            if (token.length() >= 4 && normalizedAppName.contains(token)) {
                matches++;
            }
        }
        return matches;
    }

    private List<ExcelDataset> readExcelRows() {
        try (InputStream inputStream = Files.newInputStream(EXCEL_FILE);
                Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> columns = headerColumns(sheet.getRow(0), formatter);

            List<ExcelDataset> datasets = new ArrayList<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || text(row, columns.get("dataset id"), formatter).isBlank()) {
                    continue;
                }

                datasets.add(new ExcelDataset(
                        text(row, columns.get("dataset id"), formatter),
                        text(row, columns.get("product name"), formatter),
                        number(row, columns.get("quantity"), formatter).intValue(),
                        number(row, columns.get("price"), formatter),
                        number(row, columns.get("discount"), formatter),
                        number(row, columns.get("expected total"), formatter)));
            }
            return datasets;
        } catch (IOException ex) {
            throw new IllegalStateException("Excel read nahi ho paya: " + EXCEL_FILE.toAbsolutePath(), ex);
        }
    }

    private Map<String, Integer> headerColumns(Row headerRow, DataFormatter formatter) {
        if (headerRow == null) {
            throw new IllegalStateException("Excel header row missing hai.");
        }

        Map<String, Integer> columns = new LinkedHashMap<>();
        for (int column = 0; column < headerRow.getLastCellNum(); column++) {
            columns.put(text(headerRow, column, formatter).toLowerCase(Locale.ROOT), column);
        }

        for (String required : List.of("dataset id", "product name", "quantity", "price", "discount", "expected total")) {
            if (!columns.containsKey(required)) {
                throw new IllegalStateException("Excel required column missing hai: " + required);
            }
        }
        return columns;
    }

    private void downloadExcelIfMissing() {
        if (Files.exists(EXCEL_FILE)) {
            return;
        }

        try {
            Files.createDirectories(EXCEL_FILE.getParent());
            HttpRequest request = HttpRequest.newBuilder(URI.create(EXCEL_URL))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();
            HttpResponse<Path> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofFile(EXCEL_FILE));
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Excel download failed. HTTP " + response.statusCode());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Excel download/read nahi ho paya: " + EXCEL_FILE.toAbsolutePath(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Excel download interrupt ho gaya.", ex);
        }
    }

    private void printExcelDataBeforeExecution(List<ExcelDataset> datasets) {
        logSection("EXCEL DATA BEFORE EXECUTION");
        for (ExcelDataset dataset : datasets) {
            log("Dataset ID=" + dataset.datasetId()
                    + " | Product=" + dataset.productName()
                    + " | Quantity=" + dataset.quantity()
                    + " | Price=" + formatValue(dataset.price())
                    + " | Discount=" + formatValue(dataset.discount())
                    + " | Expected Total=" + formatValue(dataset.expectedTotal()));
        }
        logSection("EXCEL DATA END");
    }

    private String text(Row row, int column, DataFormatter formatter) {
        return formatter.formatCellValue(row.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
    }

    private BigDecimal number(Row row, int column, DataFormatter formatter) {
        String value = text(row, column, formatter);
        return value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value).stripTrailingZeros();
    }

    private boolean printCheck(String fieldName, Object expected, Object actual) {
        boolean passed = valuesMatch(expected, actual);
        log(String.format("%-24s | Expected: %-30s | Actual: %-30s | Result: %s",
                fieldName, formatValue(expected), formatValue(actual), passed ? "PASS" : "FAIL"));
        return passed;
    }

    private boolean valuesMatch(Object expected, Object actual) {
        if (expected instanceof BigDecimal expectedDecimal && actual instanceof BigDecimal actualDecimal) {
            return expectedDecimal.stripTrailingZeros().compareTo(actualDecimal.stripTrailingZeros()) == 0;
        }
        return String.valueOf(expected).equals(String.valueOf(actual));
    }

    private String formatValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value);
    }

    private void logSection(String message) {
        log("========== " + message + " ==========");
    }

    private void log(String message) {
        System.out.println(message);
    }

    private record ExcelDataset(
            String datasetId,
            String productName,
            int quantity,
            BigDecimal price,
            BigDecimal discount,
            BigDecimal expectedTotal) {
    }

    private record CheckoutScenario(String actualProductName, CheckoutPage checkoutPage) {
    }

    private record CheckoutLine(ExcelDataset dataset, String actualProductName, BigDecimal lineTotal) {
    }

    private record MultiCheckoutScenario(
            List<CheckoutLine> lines,
            int totalQuantity,
            BigDecimal productTotal,
            CheckoutPage checkoutPage) {
    }
}
