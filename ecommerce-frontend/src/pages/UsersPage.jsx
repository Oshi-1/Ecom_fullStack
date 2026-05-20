import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAdminUsers } from "../api/adminApi";
import styles from "../styles/dashboard.module.css";

export default function UsersPage() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadUsers = async () => {
      try {
        const res = await getAdminUsers();
        setUsers(Array.isArray(res.data) ? res.data : []);
      } catch (err) {
        setError(err.response?.data?.error || "Users could not be loaded");
      } finally {
        setLoading(false);
      }
    };

    loadUsers();
  }, []);

  const totals = useMemo(() => {
    return users.reduce(
      (summary, user) => {
        if (user.role === "ADMIN") {
          summary.admins += 1;
        } else {
          summary.customers += 1;
        }
        return summary;
      },
      { admins: 0, customers: 0 }
    );
  }, [users]);

  return (
    <div className={styles.usersPage}>
      <header className={styles.usersHeader}>
        <div>
          <span className={styles.adminBadge}>Admin</span>
          <h1 className={styles.title}>Users</h1>
        </div>
        <div className={styles.actions}>
          <button type="button" onClick={() => navigate("/dashboard")}>
            Dashboard
          </button>
          <button type="button" onClick={() => navigate("/products")}>
            Products
          </button>
        </div>
      </header>

      <section className={styles.userStats}>
        <div>
          <span>Total Users</span>
          <strong>{users.length}</strong>
        </div>
        <div>
          <span>Customers</span>
          <strong>{totals.customers}</strong>
        </div>
        <div>
          <span>Admins</span>
          <strong>{totals.admins}</strong>
        </div>
      </section>

      {loading && <p className={styles.message}>Loading users...</p>}
      {!loading && error && <p className={styles.error}>{error}</p>}

      {!loading && !error && (
        <div className={styles.usersTableWrap}>
          <table className={styles.usersTable}>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Password Changes</th>
                <th>Password Updated</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.userId}>
                  <td>{user.userId}</td>
                  <td>{user.name}</td>
                  <td>{user.email}</td>
                  <td>
                    <span className={user.role === "ADMIN" ? styles.roleAdmin : styles.roleUser}>
                      {user.role}
                    </span>
                  </td>
                  <td>
                    <span className={styles.passwordCount}>{Number(user.passwordChangeCount || 0)}</span>
                  </td>
                  <td>{user.passwordUpdatedAt ? new Date(user.passwordUpdatedAt).toLocaleString() : "Not tracked"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
