import { useLocation, useNavigate } from "react-router-dom";
import styles from "../styles/products.module.css";

export default function OrderSuccessPage() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const order = state?.order;

  if (!order) {
    return (
      <div className={styles.page}>
        <div className={styles.emptyState}>
          <p>Order details are not available</p>
          <button type="button" className={styles.searchBtn} onClick={() => navigate("/my-orders")}>
            View My Orders
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.successPanel}>
        <span>Order Placed</span>
        <h1>Thank you for your order</h1>
        <p>
          Order #{order.orderId} has been created successfully and your cart has been cleared.
        </p>

        <div className={styles.successStats}>
          <div>
            <span>Items</span>
            <strong>{order.totalItems}</strong>
          </div>
          <div>
            <span>Total</span>
            <strong>Rs. {order.totalAmount}</strong>
          </div>
          <div>
            <span>Payment</span>
            <strong>{order.paymentMethod}</strong>
          </div>
        </div>

        <div className={styles.successAddress}>
          <span>Delivery Address</span>
          <p>{order.shippingAddress}</p>
          {order.contactNumber && <small>{order.contactNumber}</small>}
        </div>

        <div className={styles.topActions}>
          <button type="button" className={styles.searchBtn} onClick={() => navigate("/my-orders")}>
            View My Orders
          </button>
          <button type="button" onClick={() => navigate("/products")}>
            Shop More
          </button>
        </div>
      </div>
    </div>
  );
}
