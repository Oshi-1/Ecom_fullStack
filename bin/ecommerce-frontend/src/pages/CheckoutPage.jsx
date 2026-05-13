import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getCart } from "../api/cartApi";
import { placeOrder } from "../api/orderApi";
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
  const [cart, setCart] = useState({ items: [], totalItems: 0, totalAmount: 0 });
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

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
    return (
      cart.items.length > 0 &&
      form.houseNo.trim() &&
      form.city.trim() &&
      form.pincode.trim() &&
      form.state.trim() &&
      !submitting
    );
  }, [cart.items.length, form.city, form.houseNo, form.pincode, form.state, submitting]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    const shippingAddress = buildShippingAddress();

    if (!shippingAddress) {
      setError("Shipping address is required");
      return;
    }

    setSubmitting(true);

    try {
      await placeOrder({
        shippingAddress,
        paymentMethod: form.paymentMethod,
        contactNumber: form.contactNumber.trim(),
      });

      navigate("/my-orders", {
        state: {
          toast: { type: "success", message: "Order placed successfully" },
        },
      });
    } catch (err) {
      setError(err.response?.data?.error || "Order could not be placed");
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
          <form className={styles.checkoutForm} onSubmit={handleSubmit}>
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
                />
              </label>

              <label>
                <span>Street / Area</span>
                <input
                  type="text"
                  name="street"
                  value={form.street}
                  onChange={handleChange}
                  maxLength="160"
                />
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
                />
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
                />
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
                />
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
              />
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

            <button type="submit" className={styles.searchBtn} disabled={!canSubmit}>
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
