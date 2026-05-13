import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMyOrders } from "../api/orderApi";
import styles from "../styles/dashboard.module.css";

function formatDate(value) {
  if (!value) {
    return "Order placed";
  }

  return new Date(value).toLocaleString();
}

export default function UserOrdersPage() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadOrders = async () => {
      try {
        const res = await getMyOrders();
        setOrders(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        setError(err.response?.data?.error || "Your orders could not be loaded");
      } finally {
        setLoading(false);
      }
    };

    loadOrders();
  }, []);

  const totals = useMemo(() => {
    return orders.reduce(
      (summary, order) => {
        summary.items += Number(order.totalItems || 0);
        summary.amount += Number(order.totalAmount || 0);
        return summary;
      },
      { items: 0, amount: 0 }
    );
  }, [orders]);

  return (
    <div className={styles.usersPage}>
      <header className={styles.usersHeader}>
        <div>
          <span className={styles.userBadge}>Account</span>
          <h1 className={styles.title}>My Orders</h1>
        </div>
        <div className={styles.actions}>
          <button type="button" onClick={() => navigate("/dashboard")}>
            Dashboard
          </button>
          <button type="button" onClick={() => navigate("/products")}>
            Shop More
          </button>
        </div>
      </header>

      <section className={styles.userStats}>
        <div>
          <span>Total Orders</span>
          <strong>{orders.length}</strong>
        </div>
        <div>
          <span>Items Ordered</span>
          <strong>{totals.items}</strong>
        </div>
        <div>
          <span>Total Spent</span>
          <strong>Rs. {totals.amount.toFixed(2)}</strong>
        </div>
      </section>

      {loading && <p className={styles.message}>Loading your orders...</p>}
      {!loading && error && <p className={styles.error}>{error}</p>}

      {!loading && !error && orders.length === 0 && (
        <div className={styles.ordersEmpty}>
          <h2>No orders yet</h2>
          <p>Your previous orders will appear here after checkout.</p>
          <button type="button" onClick={() => navigate("/products")}>
            Browse Products
          </button>
        </div>
      )}

      {!loading && !error && orders.length > 0 && (
        <div className={styles.buyerOrderList}>
          {orders.map((order) => (
            <article className={styles.buyerOrderCard} key={order.orderId}>
              <div className={styles.buyerOrderTop}>
                <div>
                  <span>Order placed</span>
                  <strong>{formatDate(order.createdAt)}</strong>
                </div>
                <div>
                  <span>Total</span>
                  <strong>Rs. {order.totalAmount}</strong>
                </div>
                <div>
                  <span>Ship to</span>
                  <strong>{order.customerName}</strong>
                </div>
                <div>
                  <span>Order #{order.orderId}</span>
                  <b>{order.status}</b>
                </div>
              </div>

              <div className={styles.buyerOrderBody}>
                <div className={styles.orderDelivery}>
                  <span>Delivery Address</span>
                  <p>{order.shippingAddress}</p>
                  <small>
                    {order.paymentMethod}
                    {order.contactNumber ? ` | ${order.contactNumber}` : ""}
                  </small>
                </div>

                <div className={styles.buyerOrderItems}>
                  {order.items.map((item) => (
                    <div className={styles.buyerOrderItem} key={`${order.orderId}-${item.productId}`}>
                      <div className={styles.orderItemThumb}>
                        {item.productName?.charAt(0)?.toUpperCase() || "P"}
                      </div>
                      <div>
                        <h2>{item.productName}</h2>
                        <p>{item.category}</p>
                        <span>
                          Qty: {item.quantity} x Rs. {item.price}
                        </span>
                      </div>
                      <div className={styles.orderItemAction}>
                        <strong>Rs. {item.subtotal}</strong>
                        <button type="button" onClick={() => navigate(`/products/${item.productId}`)}>
                          View Product
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
