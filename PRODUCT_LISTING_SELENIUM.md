# Product Listing Selenium Automation

The Product Listing module contains one complete TestNG test. It creates and signs in
one user once, directly opens the Product Listing Page using the same authenticated
browser session, and executes every required validation.

The old separate search, category filter, and product-details test cases have been
removed.

## Execute

Keep MySQL running, then execute:

```powershell
.\mvnw.cmd "-Dtest=ProductListingTest" "-Dselenium.keepBrowserOpen=false" test
```

For Eclipse, right-click the saved root `testng.xml` file and select
`Run As -> TestNG Suite`. This suite contains only the complete Product Listing test.

Expected successful output:

```text
===== Product Listing complete execution started =====
Single sign-in successfully complete ho gaya
Sign-in ke baad Product Listing validations start ho gayi hain
Product Listing Page open ho gayi
Page successfully load ho gayi
Product listing section display ho raha hai
Products page par load ho gaye hain
Product count 0 se greater hai (kam se kam 1 product present hai)
Har product ko dekhne aur validate karne ke liye page niche scroll ho raha hai
Browser ne all products tak scroll karke name, image aur price validate kiya. Errors: []
Product Visibility Sabhi listed products visible hain
Har product ka Name visible hai
Har product ki Image visible hai
Har product ka Price visible hai
Koi hidden product nahi mila
Koi missing product nahi mila
===== Product Listing complete execution successfully finished =====
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
