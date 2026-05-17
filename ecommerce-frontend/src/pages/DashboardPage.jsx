import { useNavigate } from "react-router-dom";
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
  const actions = isAdmin ? adminActions : customerActions;

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
            <span>{isAdmin ? "Role" : "Account"}</span>
            <strong>{user?.role || "USER"}</strong>
            <small>{isAdmin ? "Full access enabled" : "Ready to shop"}</small>
          </div>
        </section>

        <section className={styles.statGrid}>
          <div>
            <span>{isAdmin ? "Catalogue" : "Store"}</span>
            <strong>{isAdmin ? "Live" : "Open"}</strong>
            <p>{isAdmin ? "Products can be edited anytime." : "Browse the latest available products."}</p>
          </div>
          <div>
            <span>{isAdmin ? "Orders" : "Checkout"}</span>
            <strong>{isAdmin ? "Track" : "Fast"}</strong>
            <p>{isAdmin ? "Review every placed customer order." : "Cart and checkout are one click away."}</p>
          </div>
          <div>
            <span>{isAdmin ? "Users" : "History"}</span>
            <strong>{isAdmin ? "Manage" : "Saved"}</strong>
            <p>{isAdmin ? "Customer records stay accessible." : "Your previous orders stay organized."}</p>
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
              <small>{action.text}</small>
            </button>
          ))}
        </section>
      </main>
    </div>
  );
}
