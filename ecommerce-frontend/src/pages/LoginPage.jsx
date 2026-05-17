import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { loginUser } from "../api/authApi";
import { useAuth } from "../context/useAuth";
import styles from "../styles/auth.module.css";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: "" });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrors({});
    try {
      const res = await loginUser(form);
      const { token, ...userData } = res.data;
      if (!token) {
        setErrors({ general: "Login succeeded, but no JWT token was returned." });
        return;
      }
      login(userData, token);
      navigate("/dashboard");
    } catch (err) {
      const data = err.response?.data;
      if (data?.error) {
        setErrors({ general: data.error });
      } else if (data && typeof data === "object") {
        setErrors(data);
      } else {
        setErrors({ general: "Invalid email or password" });
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.authPage}>
      <section className={styles.authCard}>
        <span className={styles.formEyebrow}>Cartora</span>
        <h1 className={styles.title}>Sign in</h1>
        <p className={styles.subtitle}>Enter your details to continue shopping.</p>
        {errors.general && <p className={styles.errorBanner}>{errors.general}</p>}
        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.field}>
            <label>Email</label>
            <input name="email" type="email" placeholder="you@example.com" value={form.email} onChange={handleChange} />
            {errors.email && <span className={styles.error}>{errors.email}</span>}
          </div>
          <div className={styles.field}>
            <label>Password</label>
            <input name="password" type="password" placeholder="Your password" value={form.password} onChange={handleChange} />
            {errors.password && <span className={styles.error}>{errors.password}</span>}
          </div>
          <button type="submit" className={styles.btn} disabled={loading}>
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>
        <p className={styles.switchLink}>
          No account yet? <Link to="/register">Create account</Link>
        </p>
      </section>
    </div>
  );
}
