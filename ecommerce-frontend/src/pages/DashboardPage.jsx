import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAdminOrders, getAdminUsers } from "../api/adminApi";
import { getCart } from "../api/cartApi";
import { getMyOrders } from "../api/orderApi";
import { useAuth } from "../context/useAuth";
import styles from "../styles/dashboard.module.css";

const customerActions = [
  { label: "Browse Store", text: "Explore the full product catalogue.", path: "/products" },
  { label: "Open Cart", text: "Review saved items before checkout.", path: "/cart" },
  { label: "Track Orders", text: "See your latest order history.", path: "/my-orders" },
];

const adminActions = [
  { label: "Products", text: "Update pricing, stock, and product details.", path: "/products" },
  { label: "Add Product", text: "Publish a new item to the storefront.", path: "/products/new" },
  { label: "Users", text: "Review customer and admin accounts.", path: "/users" },
  { label: "Orders", text: "Monitor sales and fulfillment records.", path: "/orders" },
];

export default function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const isAdmin = user?.role === "ADMIN";
  const [metrics, setMetrics] = useState({
    cartItems: 0,
    orderCount: 0,
    orderItems: 0,
    revenue: 0,
    userCount: 0,
  });
  const [metricsLoading, setMetricsLoading] = useState(true);

  useEffect(() => {
    let active = true;

    const loadMetrics = async () => {
      setMetricsLoading(true);

      try {
        if (isAdmin) {
          const [ordersRes, usersRes] = await Promise.all([getAdminOrders(), getAdminUsers()]);
          const orders = Array.isArray(ordersRes.data) ? ordersRes.data : [];
          const users = Array.isArray(usersRes.data) ? usersRes.data : [];

          if (active) {
            setMetrics({
              cartItems: 0,
              orderCount: orders.length,
              orderItems: orders.reduce((total, order) => total + Number(order.totalItems || 0), 0),
              revenue: orders.reduce((total, order) => total + Number(order.totalAmount || 0), 0),
              userCount: users.length,
            });
          }
          return;
        }

        const [cartRes, ordersRes] = await Promise.all([getCart(), getMyOrders()]);
        const cart = cartRes.data || {};
        const orders = Array.isArray(ordersRes.data) ? ordersRes.data : [];

        if (active) {
          setMetrics({
            cartItems: Number(cart.totalItems || 0),
            orderCount: orders.length,
            orderItems: orders.reduce((total, order) => total + Number(order.totalItems || 0), 0),
            revenue: orders.reduce((total, order) => total + Number(order.totalAmount || 0), 0),
            userCount: 0,
          });
        }
      } catch {
        if (active) {
          setMetrics({
            cartItems: 0,
            orderCount: 0,
            orderItems: 0,
            revenue: 0,
            userCount: 0,
          });
        }
      } finally {
        if (active) {
          setMetricsLoading(false);
        }
      }
    };

    loadMetrics();

    return () => {
      active = false;
    };
  }, [isAdmin]);

  const actions = useMemo(() => {
    const baseActions = isAdmin ? adminActions : customerActions;

    return baseActions.map((action) => {
      if (action.path === "/cart") {
        return { ...action, count: metrics.cartItems, countLabel: "cart items" };
      }

      if (action.path === "/my-orders" || action.path === "/orders") {
        return { ...action, count: metrics.orderCount, countLabel: "orders" };
      }

      if (action.path === "/users") {
        return { ...action, count: metrics.userCount, countLabel: "users" };
      }

      return action;
    });
  }, [isAdmin, metrics.cartItems, metrics.orderCount, metrics.userCount]);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className={`${styles.layout} ${isAdmin ? styles.adminLayout : ""}`}>
      <header className={styles.navbar}>
        <div className={styles.brandBlock}>
          <span className={styles.brandMark}>{isAdmin ? "A" : "C"}</span>
          <div>
            <h2 className={styles.logo}>{isAdmin ? "Command Desk" : "Cartora"}</h2>
            <p>{isAdmin ? "Store operations" : "Smart shopping"}</p>
          </div>
        </div>

        <div className={styles.profileCard}>
          <span>{user?.name?.charAt(0)?.toUpperCase() || "U"}</span>
          <div>
            <strong>{user?.name || "User"}</strong>
            <small>{isAdmin ? "Administrator" : "Customer Account"}</small>
          </div>
        </div>

        <nav className={styles.nav}>
          {actions.map((action) => (
            <button key={action.path} onClick={() => navigate(action.path)}>
              <span>{action.label}</span>
              {typeof action.count === "number" && (
                <b className={styles.navCount}>{metricsLoading ? "..." : action.count}</b>
              )}
            </button>
          ))}
          <button className={styles.logoutBtn} onClick={handleLogout}>
            <span>Logout</span>
          </button>
        </nav>
      </header>

      <main className={styles.content}>
        <section className={styles.heroPanel}>
          <div>
            <span className={isAdmin ? styles.adminBadge : styles.userBadge}>
              {isAdmin ? "Admin Panel" : "Customer Dashboard"}
            </span>
            <h1 className={styles.title}>
              {isAdmin ? "Run your store from one place." : `Welcome back, ${user?.name || "shopper"}.`}
            </h1>
            <p>
              {isAdmin
                ? "Manage catalogue, customers, and order activity with a cleaner control room."
                : "Jump back into products, cart, and recent orders from a more polished shopping hub."}
            </p>
          </div>
          <div className={styles.heroMetric}>
            <span>{isAdmin ? "All Orders" : "My Orders"}</span>
            <strong>{metricsLoading ? "..." : metrics.orderCount}</strong>
            <small>{isAdmin ? "Across every customer" : "Only your account"}</small>
          </div>
        </section>

        <section className={styles.statGrid}>
          <div>
            <span>{isAdmin ? "Catalogue" : "Store"}</span>
            <strong>{isAdmin ? "Live" : "Open"}</strong>
            <p>{isAdmin ? "Products can be edited anytime." : "Browse the latest available products."}</p>
          </div>
          <div>
            <span>{isAdmin ? "Orders" : "Cart Items"}</span>
            <strong>{metricsLoading ? "..." : isAdmin ? metrics.orderCount : metrics.cartItems}</strong>
            <p>{isAdmin ? "Review every placed customer order." : "Your cart count stays tied to this account."}</p>
          </div>
          <div>
            <span>{isAdmin ? "Users" : "Orders"}</span>
            <strong>{metricsLoading ? "..." : isAdmin ? metrics.userCount : metrics.orderCount}</strong>
            <p>{isAdmin ? "Customer records stay accessible." : "Only orders placed by you are shown."}</p>
          </div>
        </section>

        <section className={styles.actionGrid}>
          {actions.map((action, index) => (
            <button
              type="button"
              className={styles.actionTile}
              key={action.path}
              onClick={() => navigate(action.path)}
            >
              <span>{String(index + 1).padStart(2, "0")}</span>
              <strong>{action.label}</strong>
              {typeof action.count === "number" && (
                <em className={styles.actionCount}>
                  {metricsLoading ? "..." : action.count} {action.countLabel}
                </em>
              )}
              <small>{action.text}</small>
            </button>
          ))}
        </section>
      </main>
    </div>
  );
}
