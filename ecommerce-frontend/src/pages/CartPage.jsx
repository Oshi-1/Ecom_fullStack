import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { clearCart, getCart, removeCartItem, updateCartItem } from "../api/cartApi";
import { useAuth } from "../context/useAuth";
import { useNotification } from "../context/useNotification";
import { handleProductImageError, productImageFallback } from "../utils/productImage";
import styles from "../styles/products.module.css";

export default function CartPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showSuccess, showError } = useNotification();
  const [cart, setCart] = useState({ items: [], totalItems: 0, totalAmount: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const isAdmin = user?.role === "ADMIN";

  const loadCart = async () => {
    setError("");

    try {
      const res = await getCart();
      setCart(res.data);
    } catch {
      setError("Failed to load cart");
      showError("Failed to load cart.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAdmin) {
      navigate("/products", {
        replace: true,
        state: {
          toast: { type: "error", message: "Admin users manage products and cannot use the cart" },
        },
      });
      return undefined;
    }

    let active = true;

    const loadInitialCart = async () => {
      try {
        const res = await getCart();
        if (active) {
          setCart(res.data);
        }
      } catch {
        if (active) {
          setError("Failed to load cart");
          showError("Failed to load cart.");
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    loadInitialCart();

    return () => {
      active = false;
    };
  }, [isAdmin, navigate]);

  const handleQuantityChange = async (item, quantity) => {
    const nextQuantity = Number(quantity);

    if (nextQuantity < 1 || nextQuantity > item.stock) {
      return;
    }

    try {
      const res = await updateCartItem(item.cartItemId, nextQuantity);
      setCart(res.data);
      showSuccess("Cart quantity updated.");
    } catch (err) {
      const message = err.response?.data?.error || "Quantity could not be updated";
      setError(message);
      showError(message);
    }
  };

  const handleRemove = async (cartItemId) => {
    try {
      const res = await removeCartItem(cartItemId);
      setCart(res.data);
      showSuccess("Item removed from cart.");
    } catch {
      setError("Item could not be removed");
      showError("Item could not be removed.");
    }
  };

  const handleClear = async () => {
    try {
      await clearCart();
      await loadCart();
      showSuccess("Cart cleared successfully.");
    } catch {
      setError("Cart could not be cleared");
      showError("Cart could not be cleared.");
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.topBar}>
        <h1>Cart</h1>
        <div className={styles.topActions}>
          <button type="button" onClick={() => navigate("/products")}>
            Products
          </button>
          <button type="button" onClick={() => navigate("/dashboard")}>
            Dashboard
          </button>
        </div>
      </div>

      {loading && <p className={styles.message}>Loading...</p>}
      {error && <p className={styles.error}>{error}</p>}

      {!loading && cart.items.length === 0 && (
        <div className={styles.emptyState}>
          <p>Your cart is empty</p>
          <button type="button" className={styles.searchBtn} onClick={() => navigate("/products")}>
            Browse Products
          </button>
        </div>
      )}

      {!loading && cart.items.length > 0 && (
        <div className={styles.cartLayout}>
          <div className={styles.cartItems}>
            {cart.items.map((item) => (
              <article className={styles.cartItem} key={item.cartItemId}>
                <img
                  src={item.imageUrl || productImageFallback(item.name)}
                  alt={item.name}
                  onError={(event) => handleProductImageError(event, item.name)}
                />

                <div>
                  <h3>{item.name}</h3>
                  <p>{item.category}</p>
                  <strong>Rs. {item.price}</strong>
                </div>

                <input
                  type="number"
                  min="1"
                  max={item.stock}
                  value={item.quantity}
                  onChange={(event) => handleQuantityChange(item, event.target.value)}
                />

                <strong>Rs. {item.subtotal}</strong>

                <button type="button" onClick={() => handleRemove(item.cartItemId)}>
                  Remove
                </button>
              </article>
            ))}
          </div>

          <aside className={styles.cartSummary}>
            <h2>Summary</h2>
            <p>
              <span>Items</span>
              <strong>{cart.totalItems}</strong>
            </p>
            <p>
              <span>Total</span>
              <strong>Rs. {cart.totalAmount}</strong>
            </p>
            <button type="button" className={styles.searchBtn} onClick={() => navigate("/checkout")}>
              Checkout
            </button>
            <button type="button" className={styles.resetBtn} onClick={handleClear}>
              Clear Cart
            </button>
          </aside>
        </div>
      )}
    </div>
  );
}
