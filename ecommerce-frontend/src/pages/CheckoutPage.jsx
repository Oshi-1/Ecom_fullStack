import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { clearCart, getCart } from "../api/cartApi";
import { placeOrder } from "../api/orderApi";
import { useAuth } from "../context/useAuth";
import { useNotification } from "../context/useNotification";
import { handleProductImageError, productImageFallback } from "../utils/productImage";
import styles from "../styles/products.module.css";

const initialForm = {
  houseNo: "",
  street: "",
  city: "",
  pincode: "",
  state: "",
  paymentMethod: "COD",
  contactNumber: "",
};

export default function CheckoutPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showError } = useNotification();
  const [cart, setCart] = useState({ items: [], totalItems: 0, totalAmount: 0 });
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  const buildShippingAddress = () => {
    return [
      form.houseNo.trim(),
      form.street.trim(),
      form.city.trim(),
      form.pincode.trim(),
      form.state.trim(),
    ]
      .filter(Boolean)
      .join(", ");
  };

  useEffect(() => {
    let active = true;

    const loadCart = async () => {
      try {
        const res = await getCart();
        if (active) {
          setCart(res.data);
        }
      } catch {
        if (active) {
          setError("Checkout could not be loaded");
          showError("Checkout could not be loaded.");
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadCart();

    return () => {
      active = false;
    };
  }, []);

  const canSubmit = useMemo(() => {
    return cart.items.length > 0 && !submitting;
  }, [cart.items.length, submitting]);

  const getApiError = (err) => {
    const data = err.response?.data;

    if (data?.error) {
      return data.error;
    }

    if (data && typeof data === "object") {
      return Object.values(data).find(Boolean) || "Order could not be placed";
    }

    return "Order could not be placed";
  };

  const validateForm = () => {
    const errors = {};
    let hasBlankRequiredField = false;

    if (!form.houseNo.trim()) {
      hasBlankRequiredField = true;
      errors.houseNo = "House / Flat No. is required";
    }

    if (!form.street.trim()) {
      hasBlankRequiredField = true;
      errors.street = "Street / Area is required";
    }

    if (!form.city.trim()) {
      hasBlankRequiredField = true;
      errors.city = "City is required";
    }

    if (!form.pincode.trim()) {
      hasBlankRequiredField = true;
      errors.pincode = "Pincode is required";
    } else if (!/^[0-9]{6}$/.test(form.pincode.trim())) {
      errors.pincode = "Enter a valid 6 digit pincode";
    }

    if (!form.state.trim()) {
      hasBlankRequiredField = true;
      errors.state = "State is required";
    }

    if (!form.contactNumber.trim()) {
      hasBlankRequiredField = true;
      errors.contactNumber = "Contact number is required";
    } else if (!/^[0-9+\-\s()]{7,20}$/.test(form.contactNumber.trim())) {
      errors.contactNumber = "Enter a valid contact number";
    }

    if (!cart.items.length) {
      errors.cart = "Your cart is empty";
    }

    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      showError(hasBlankRequiredField ? "Required fields cannot be left blank" : Object.values(errors)[0]);
    }
    return Object.keys(errors).length === 0;
  };

  const buildCheckoutPayload = (shippingAddress) => ({
    userDetails: {
      name: user?.name || "Customer",
      email: user?.email || "",
    },
    shippingAddress,
    cartItems: cart.items.map((item) => ({
      productId: item.productId,
      name: item.name,
      quantity: item.quantity,
      subtotal: item.subtotal,
    })),
    totalAmount: cart.totalAmount,
    paymentMethod: form.paymentMethod,
    contactNumber: form.contactNumber.trim(),
  });

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (!validateForm()) {
      return;
    }

    const shippingAddress = buildShippingAddress();

    if (!shippingAddress) {
      setError("Shipping address is required");
      showError("Shipping address is required.");
      return;
    }

    setSubmitting(true);

    try {
      const res = await placeOrder(buildCheckoutPayload(shippingAddress));
      await clearCart();

      navigate("/order-success", {
        replace: true,
        state: {
          order: res.data,
          toast: { type: "success", message: "Order placed successfully" },
        },
      });
    } catch (err) {
      const message = getApiError(err);
      setError(message);
      showError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.topBar}>
        <h1>Checkout</h1>
        <div className={styles.topActions}>
          <button type="button" onClick={() => navigate("/cart")}>
            Cart
          </button>
          <button type="button" onClick={() => navigate("/products")}>
            Products
          </button>
        </div>
      </div>

      {loading && <p className={styles.message}>Loading checkout...</p>}
      {!loading && error && <p className={styles.errorCompact}>{error}</p>}

      {!loading && cart.items.length === 0 && (
        <div className={styles.emptyState}>
          <p>Your cart is empty</p>
          <button type="button" className={styles.searchBtn} onClick={() => navigate("/products")}>
            Browse Products
          </button>
        </div>
      )}

      {!loading && cart.items.length > 0 && (
        <div className={styles.checkoutLayout}>
          <form className={styles.checkoutForm} onSubmit={handleSubmit} noValidate>
            <div className={styles.addressGrid}>
              <label>
                <span>House / Flat No.</span>
                <input
                  type="text"
                  name="houseNo"
                  value={form.houseNo}
                  onChange={handleChange}
                  maxLength="120"
                  required
                  aria-invalid={Boolean(fieldErrors.houseNo)}
                />
                {fieldErrors.houseNo && <span className={styles.fieldError}>{fieldErrors.houseNo}</span>}
              </label>

              <label>
                <span>Street / Area</span>
                <input
                  type="text"
                  name="street"
                  value={form.street}
                  onChange={handleChange}
                  maxLength="160"
                  required
                  aria-invalid={Boolean(fieldErrors.street)}
                />
                {fieldErrors.street && <span className={styles.fieldError}>{fieldErrors.street}</span>}
              </label>

              <label>
                <span>City</span>
                <input
                  type="text"
                  name="city"
                  value={form.city}
                  onChange={handleChange}
                  maxLength="80"
                  required
                  aria-invalid={Boolean(fieldErrors.city)}
                />
                {fieldErrors.city && <span className={styles.fieldError}>{fieldErrors.city}</span>}
              </label>

              <label>
                <span>Pincode</span>
                <input
                  type="text"
                  name="pincode"
                  value={form.pincode}
                  onChange={handleChange}
                  inputMode="numeric"
                  pattern="[0-9]{6}"
                  maxLength="6"
                  required
                  aria-invalid={Boolean(fieldErrors.pincode)}
                />
                {fieldErrors.pincode && <span className={styles.fieldError}>{fieldErrors.pincode}</span>}
              </label>

              <label>
                <span>State</span>
                <input
                  type="text"
                  name="state"
                  value={form.state}
                  onChange={handleChange}
                  maxLength="80"
                  required
                  aria-invalid={Boolean(fieldErrors.state)}
                />
                {fieldErrors.state && <span className={styles.fieldError}>{fieldErrors.state}</span>}
              </label>
            </div>

            <label>
              <span>Contact Number</span>
              <input
                type="tel"
                name="contactNumber"
                value={form.contactNumber}
                onChange={handleChange}
                maxLength="20"
                required
                aria-invalid={Boolean(fieldErrors.contactNumber)}
              />
              {fieldErrors.contactNumber && (
                <span className={styles.fieldError}>{fieldErrors.contactNumber}</span>
              )}
            </label>

            <fieldset className={styles.paymentOptions}>
              <legend>Payment Method</legend>
              <label>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="COD"
                  checked={form.paymentMethod === "COD"}
                  onChange={handleChange}
                />
                <span>Cash on Delivery</span>
              </label>
              <label>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="UPI"
                  checked={form.paymentMethod === "UPI"}
                  onChange={handleChange}
                />
                <span>UPI</span>
              </label>
              <label>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="CARD"
                  checked={form.paymentMethod === "CARD"}
                  onChange={handleChange}
                />
                <span>Card</span>
              </label>
            </fieldset>

            {fieldErrors.cart && <p className={styles.errorCompact}>{fieldErrors.cart}</p>}

            <button type="submit" className={styles.searchBtn} disabled={!canSubmit} aria-busy={submitting}>
              {submitting ? "Placing Order..." : "Place Order"}
            </button>
          </form>

          <aside className={styles.checkoutSummary}>
            <h2>Order Summary</h2>
            <div className={styles.checkoutItems}>
              {cart.items.map((item) => (
                <div className={styles.checkoutItem} key={item.cartItemId}>
                  <img
                    src={item.imageUrl || productImageFallback(item.name)}
                    alt={item.name}
                    onError={(event) => handleProductImageError(event, item.name)}
                  />
                  <div>
                    <strong>{item.name}</strong>
                    <span>
                      {item.quantity} x Rs. {item.price}
                    </span>
                  </div>
                  <b>Rs. {item.subtotal}</b>
                </div>
              ))}
            </div>
            <p>
              <span>Items</span>
              <strong>{cart.totalItems}</strong>
            </p>
            <p>
              <span>Total</span>
              <strong>Rs. {cart.totalAmount}</strong>
            </p>
          </aside>
        </div>
      )}
    </div>
  );
}
