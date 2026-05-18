import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { forgotPassword } from "../api/authApi";
import styles from "../styles/auth.module.css";

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", newPassword: "", confirmPassword: "" });
  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
    setErrors({ ...errors, [event.target.name]: "", general: "" });
    setSuccess("");
  };

  const handleGeneratePassword = () => {
    const letters = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    const numbers = "23456789";
    const symbols = "@#$%";
    const all = `${letters}${numbers}${symbols}`;
    const password = Array.from({ length: 10 }, () => all[Math.floor(Math.random() * all.length)]).join("");

    setForm({ ...form, newPassword: password, confirmPassword: password });
    setErrors({ ...errors, newPassword: "", confirmPassword: "", general: "" });
    setSuccess("");
    setShowPassword(true);
  };

  const validate = () => {
    const nextErrors = {};

    if (!form.email.trim()) {
      nextErrors.email = "Email is required";
    }

    if (!form.newPassword) {
      nextErrors.newPassword = "New password is required";
    } else if (form.newPassword.length < 6) {
      nextErrors.newPassword = "Password must be at least 6 characters";
    }

    if (form.confirmPassword !== form.newPassword) {
      nextErrors.confirmPassword = "Passwords do not match";
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!validate()) {
      return;
    }

    setLoading(true);
    setErrors({});
    setSuccess("");

    try {
      await forgotPassword({
        email: form.email.trim(),
        newPassword: form.newPassword,
      });
      setSuccess("Password reset successfully. Sign in with your new password.");
      setTimeout(() => navigate("/login"), 1200);
    } catch (err) {
      const data = err.response?.data;

      if (data?.error) {
        setErrors({ general: data.error });
      } else if (data && typeof data === "object") {
        setErrors(data);
      } else if (err.response?.status === 403) {
        setErrors({ general: "Reset request was blocked. Please refresh and try again." });
      } else if (!err.response) {
        setErrors({ general: "Backend is not reachable. Please make sure the server is running." });
      } else {
        setErrors({ general: "Password could not be reset" });
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.authPage}>
      <section className={styles.authCard}>
        <span className={styles.formEyebrow}>Cartora</span>
        <h1 className={styles.title}>Reset password</h1>
        <p className={styles.subtitle}>Enter your account email and create a new password.</p>

        {errors.general && <p className={styles.errorBanner}>{errors.general}</p>}
        {success && <p className={styles.successBanner}>{success}</p>}

        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.field}>
            <label>Email</label>
            <input
              name="email"
              type="email"
              placeholder="you@example.com"
              value={form.email}
              onChange={handleChange}
            />
            {errors.email && <span className={styles.error}>{errors.email}</span>}
          </div>

          <div className={styles.field}>
            <div className={styles.labelRow}>
              <label>New password</label>
              <button type="button" onClick={handleGeneratePassword}>
                Generate
              </button>
            </div>
            <input
              name="newPassword"
              type={showPassword ? "text" : "password"}
              placeholder="Create new password"
              value={form.newPassword}
              onChange={handleChange}
            />
            {errors.newPassword && <span className={styles.error}>{errors.newPassword}</span>}
          </div>

          <div className={styles.field}>
            <label>Confirm password</label>
            <input
              name="confirmPassword"
              type={showPassword ? "text" : "password"}
              placeholder="Confirm new password"
              value={form.confirmPassword}
              onChange={handleChange}
            />
            {errors.confirmPassword && <span className={styles.error}>{errors.confirmPassword}</span>}
          </div>

          <button type="submit" className={styles.btn} disabled={loading}>
            {loading ? "Resetting..." : "Reset password"}
          </button>
        </form>

        <p className={styles.switchLink}>
          Remembered it? <Link to="/login">Sign in</Link>
        </p>
      </section>
    </div>
  );
}
