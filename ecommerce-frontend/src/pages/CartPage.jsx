import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { clearCart, getCart, removeCartItem, updateCartItem, updateCartItemByProduct } from "../api/cartApi";
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
  const [updatingItemId, setUpdatingItemId] = useState(null);
  const isAdmin = user?.role === "ADMIN";

  const toNumber = (value) => Number(value || 0);

  const getLineTotal = (item) => toNumber(item.price) * toNumber(item.quantity);

  const displayCart = {
    ...cart,
    totalItems: cart.items.reduce((total, item) => total + toNumber(item.quantity), 0),
    totalAmount: cart.items.reduce((total, item) => total + getLineTotal(item), 0),
  };

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
  }, [isAdmin, navigate, showError]);

  const handleQuantityChange = async (item, quantity) => {
    const nextQuantity = Number(quantity);

    if (Number.isNaN(nextQuantity) || nextQuantity < 1 || nextQuantity > item.stock) {
      return;
    }

    const cartItemId = item.cartItemId ?? item.id;
    const previousCart = cart;
    const optimisticCart = {
      ...cart,
      items: cart.items.map((cartItem) =>
        (cartItem.cartItemId ?? cartItem.id ?? cartItem.productId) ===
        (cartItemId ?? item.productId)
          ? {
              ...cartItem,
              quantity: nextQuantity,
              subtotal: toNumber(cartItem.price) * nextQuantity,
            }
          : cartItem
      ),
    };

    try {
      setError("");
      setUpdatingItemId(cartItemId ?? item.productId);
      setCart(optimisticCart);

      const res = cartItemId
        ? await updateCartItem(cartItemId, nextQuantity)
        : await updateCartItemByProduct(item.productId, nextQuantity);

      if (res.data?.items) {
        setCart(res.data);
      } else {
        await loadCart();
      }
      showSuccess("Cart quantity updated.");
    } catch (err) {
      setCart(previousCart);
      const message =
        err.response?.data?.error ||
        err.response?.data?.quantity ||
        "Quantity could not be updated";
      setError(message);
      showError(message);
    } finally {
      setUpdatingItemId(null);
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

      {!loading && displayCart.items.length === 0 && (
        <div className={styles.emptyState}>
          <p>Your cart is empty</p>
          <button type="button" className={styles.searchBtn} onClick={() => navigate("/products")}>
            Browse Products
          </button>
        </div>
      )}

      {!loading && displayCart.items.length > 0 && (
        <div className={styles.cartLayout}>
          <div className={styles.cartItems}>
            {displayCart.items.map((item) => {
              const itemId = item.cartItemId ?? item.id ?? item.productId;
              const isUpdating = updatingItemId === itemId;

              return (
                <article className={styles.cartItem} key={itemId} data-testid="cart-item">
                  <img
                    src={item.imageUrl || productImageFallback(item.name)}
                    alt={item.name}
                    onError={(event) => handleProductImageError(event, item.name)}
                  />

                  <div>
                    <h3>{item.name}</h3>
                    <p>{item.category}</p>
                    <strong data-testid="cart-item-unit-price">Rs. {item.price}</strong>
                  </div>

                  <div
                    className={styles.quantityStepper}
                    aria-label={`Quantity for ${item.name}`}
                    data-testid="cart-quantity-stepper"
                  >
                    <button
                      type="button"
                      aria-label={`Decrease ${item.name} quantity`}
                      data-testid="cart-quantity-decrease"
                      disabled={isUpdating || item.quantity <= 1}
                      onClick={() => handleQuantityChange(item, item.quantity - 1)}
                    >
                      -
                    </button>
                    <span>{item.quantity}</span>
                    <button
                      type="button"
                      aria-label={`Increase ${item.name} quantity`}
                      data-testid="cart-quantity-increase"
                      disabled={isUpdating || item.quantity >= item.stock}
                      onClick={() => handleQuantityChange(item, item.quantity + 1)}
                    >
                      +
                    </button>
                  </div>

                  <strong data-testid="cart-item-subtotal">Rs. {getLineTotal(item)}</strong>

                  <button type="button" onClick={() => handleRemove(item.cartItemId ?? item.id)}>
                    Remove
                  </button>
                </article>
              );
            })}
          </div>

          <aside className={styles.cartSummary}>
            <h2>Summary</h2>
            <p>
              <span>Items</span>
              <strong>{displayCart.totalItems}</strong>
            </p>
            <p>
              <span>Total</span>
              <strong>Rs. {displayCart.totalAmount}</strong>
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
