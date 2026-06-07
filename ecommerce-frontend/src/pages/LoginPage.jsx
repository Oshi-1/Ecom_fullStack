import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { loginUser } from "../api/authApi";
import PasswordInput from "../components/PasswordInput";
import { useAuth } from "../context/useAuth";
import { useNotification } from "../context/useNotification";
import styles from "../styles/auth.module.css";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { showSuccess, showError } = useNotification();
  const [form, setForm] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [authError, setAuthError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setErrors((current) => ({ ...current, [e.target.name]: "" }));
    if (e.target.name === "email" || e.target.name === "password") {
      setAuthError("");
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrors({});
    try {
      const res = await loginUser(form);
      const { token, ...userData } = res.data;
      if (!token) {
        setAuthError("Login succeeded, but no JWT token was returned.");
        return;
      }
      setAuthError("");
      login(userData, token);
      showSuccess("Login successfully. Welcome back to Cartora.");
      navigate("/dashboard");
    } catch (err) {
      const data = err.response?.data;
      const status = err.response?.status;
      const errorText = String(data?.error || "").toLowerCase();

      if (status === 401 || status === 403 || errorText.includes("bad credentials")) {
        setAuthError("Invalid email or password");
        showError("Incorrect email or password.");
      } else if (data?.error) {
        setAuthError(data.error);
        showError(data.error);
      } else if (data && typeof data === "object") {
        setErrors(data);
        showError(Object.values(data)[0] || "Please check the highlighted fields.");
      } else {
        setAuthError("Invalid email or password");
        showError("Incorrect email or password.");
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
        {authError && <p className={styles.errorBanner}>{authError}</p>}
        {errors.general && <p className={styles.errorBanner}>{errors.general}</p>}
        <form onSubmit={handleSubmit} className={styles.form} autoComplete="on">
          <div className={styles.field}>
            <label>Email</label>
            <input
              name="email"
              id="login-email"
              type="email"
              placeholder="you@example.com"
              value={form.email}
              onChange={handleChange}
              autoComplete="username"
              autoCorrect="off"
              spellCheck="false"
            />
            {errors.email && <span className={styles.error}>{errors.email}</span>}
          </div>
          <div className={styles.field}>
            <div className={styles.labelRow}>
              <label>Password</label>
              <Link to="/forgot-password">Forgot password?</Link>
            </div>
            <PasswordInput
              name="password"
              id="login-password"
              placeholder="Your password"
              value={form.password}
              onChange={handleChange}
              autoComplete="current-password"
              visible={showPassword}
              onToggle={() => setShowPassword((current) => !current)}
              className={styles.passwordInput}
              buttonClassName={styles.passwordToggle}
            />
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
