import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import styles from "../styles/dashboard.module.css";

export default function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const isAdmin = user?.role === "ADMIN";

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className={`${styles.layout} ${isAdmin ? styles.adminLayout : ""}`}>
      <aside className={styles.sidebar}>
        <h2 className={styles.logo}>{isAdmin ? "Admin Panel" : "Ecommerce Web"}</h2>
        {isAdmin && <span className={styles.adminBadge}>Admin Management</span>}

        <nav className={styles.nav}>
          <button onClick={() => navigate("/products")}>
            {isAdmin ? "Manage Products" : "Products"}
          </button>

          {!isAdmin && (
            <button onClick={() => navigate("/cart")}>
              Cart
            </button>
          )}

          {!isAdmin && (
            <button onClick={() => navigate("/my-orders")}>
              My Orders
            </button>
          )}

          {isAdmin && (
            <button onClick={() => navigate("/products/new")}>
              Add Product
            </button>
          )}

          {isAdmin && (
            <button onClick={() => navigate("/users")}>
              Users
            </button>
          )}

          {isAdmin && (
            <button onClick={() => navigate("/orders")}>
              Order History
            </button>
          )}

          <button onClick={handleLogout}>
            Logout
          </button>
        </nav>
      </aside>

      <main className={styles.content}>
        <h1 className={styles.title}>{isAdmin ? "Admin Dashboard" : "Dashboard"}</h1>

        <div className={styles.card}>
          <h3>{isAdmin ? "Welcome back, Admin" : "Welcome back"}</h3>
          <p><strong>Name:</strong> {user?.name}</p>
          <p><strong>Role:</strong> {user?.role}</p>
        </div>

        <div className={styles.actions}>
          <button onClick={() => navigate("/products")}>
            {isAdmin ? "Manage Products" : "Browse Products"}
          </button>
          {!isAdmin && (
            <button onClick={() => navigate("/cart")}>
              View Cart
            </button>
          )}
          {!isAdmin && (
            <button onClick={() => navigate("/my-orders")}>
              My Orders
            </button>
          )}
          {isAdmin && (
            <button onClick={() => navigate("/users")}>
              Users
            </button>
          )}
          {isAdmin && (
            <button onClick={() => navigate("/orders")}>
              Order History
            </button>
          )}
        </div>
      </main>
    </div>
  );
}
