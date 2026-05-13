import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAdminOrders } from "../api/adminApi";
import styles from "../styles/dashboard.module.css";

function formatDate(value) {
  if (!value) {
    return "New order";
  }

  return new Date(value).toLocaleString();
}

export default function OrderHistoryPage() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadOrders = async () => {
      try {
        const res = await getAdminOrders();
        setOrders(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        setError(err.response?.data?.error || "Order history could not be loaded");
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
        summary.revenue += Number(order.totalAmount || 0);
        return summary;
      },
      { items: 0, revenue: 0 }
    );
  }, [orders]);

  return (
    <div className={styles.usersPage}>
      <header className={styles.usersHeader}>
        <div>
          <span className={styles.adminBadge}>Admin</span>
          <h1 className={styles.title}>Order History</h1>
        </div>
        <div className={styles.actions}>
          <button type="button" onClick={() => navigate("/dashboard")}>
            Dashboard
          </button>
          <button type="button" onClick={() => navigate("/users")}>
            Users
          </button>
        </div>
      </header>

      <section className={styles.userStats}>
        <div>
          <span>Total Orders</span>
          <strong>{orders.length}</strong>
        </div>
        <div>
          <span>Items Sold</span>
          <strong>{totals.items}</strong>
        </div>
        <div>
          <span>Revenue</span>
          <strong>Rs. {totals.revenue.toFixed(2)}</strong>
        </div>
      </section>

      {loading && <p className={styles.message}>Loading orders...</p>}
      {!loading && error && <p className={styles.error}>{error}</p>}

      {!loading && !error && orders.length === 0 && (
        <p className={styles.message}>No orders found</p>
      )}

      {!loading && !error && orders.length > 0 && (
        <div className={styles.orderList}>
          {orders.map((order) => (
            <article className={styles.orderCard} key={order.orderId}>
              <div className={styles.orderHead}>
                <div>
                  <span>Order #{order.orderId}</span>
                  <h2>{order.customerName}</h2>
                  <p>{order.customerEmail}</p>
                </div>
                <div>
                  <strong>Rs. {order.totalAmount}</strong>
                  <span className={styles.roleAdmin}>{order.status}</span>
                </div>
              </div>

              <div className={styles.orderMeta}>
                <span>{formatDate(order.createdAt)}</span>
                <span>{order.totalItems} items</span>
                <span>{order.paymentMethod}</span>
                {order.contactNumber && <span>{order.contactNumber}</span>}
              </div>

              <p className={styles.orderAddress}>{order.shippingAddress}</p>

              <table className={styles.orderItems}>
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Category</th>
                    <th>Qty</th>
                    <th>Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  {order.items.map((item) => (
                    <tr key={`${order.orderId}-${item.productId}`}>
                      <td>{item.productName}</td>
                      <td>{item.category}</td>
                      <td>{item.quantity}</td>
                      <td>Rs. {item.subtotal}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
