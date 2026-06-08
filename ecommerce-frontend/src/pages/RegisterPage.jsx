import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { registerUser } from "../api/authApi";
import PasswordInput from "../components/PasswordInput";
import { useAuth } from "../context/useAuth";
import { useNotification } from "../context/useNotification";
import styles from "../styles/register.module.css";

export default function RegisterPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { showSuccess, showError } = useNotification();

  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: "" });
  };

  const validateForm = () => {
    const nextErrors = {};

    if (!form.name.trim()) {
      nextErrors.name = "Name is required";
    }

    if (!form.email.trim()) {
      nextErrors.email = "Email is required";
    }

    if (!form.password) {
      nextErrors.password = "Password is required";
    } else if (form.password.length < 6) {
      nextErrors.password = "Password must be at least 6 characters";
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setErrors({});
    try {
      const res = await registerUser(form);
      const { token, ...userData } = res.data;
      if (!token) {
        setErrors({ general: "Registration succeeded, but no JWT token was returned." });
        return;
      }
      login(userData, token);
      showSuccess("Account created successfully. You are logged in now.");
      navigate("/dashboard");
    } catch (err) {
      const data = err.response?.data;
      if (data?.error) {
        setErrors({ general: data.error });
        showError(data.error);
      } else if (data && typeof data === "object") {
        setErrors(data);
        showError(Object.values(data)[0] || "Please check the highlighted fields.");
      } else {
        setErrors({ general: "Registration failed" });
        showError("Registration failed. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <section className={styles.card}>
        <span className={styles.formEyebrow}>Cartora</span>
        <h1>Sign up</h1>
        <p className={styles.subtitle}>Fill in your details to create a new customer account.</p>

        {errors.general && <p className={styles.errorBanner}>{errors.general}</p>}

        <form onSubmit={handleSubmit}>
          <label>
            <span>Full Name</span>
            <input
              name="name"
              placeholder="Enter your name"
              value={form.name}
              onChange={handleChange}
            />
            {errors.name && <small>{errors.name}</small>}
          </label>

          <label>
            <span>Email</span>
            <input
              name="email"
              type="email"
              placeholder="you@example.com"
              value={form.email}
              onChange={handleChange}
            />
            {errors.email && <small>{errors.email}</small>}
          </label>

          <label>
            <span>Password</span>
            <PasswordInput
              name="password"
              placeholder="Create password"
              value={form.password}
              onChange={handleChange}
              autoComplete="new-password"
              visible={showPassword}
              onToggle={() => setShowPassword((current) => !current)}
              className={styles.passwordInput}
              buttonClassName={styles.passwordToggle}
            />
            {errors.password && <small>{errors.password}</small>}
          </label>

          <button type="submit" disabled={loading}>
            {loading ? "Creating..." : "Create account"}
          </button>
        </form>

        <p>
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </section>
    </div>
  );
}
