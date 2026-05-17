import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { registerUser } from "../api/authApi";
import { useAuth } from "../context/useAuth";
import styles from "../styles/register.module.css";

export default function RegisterPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm] = useState({ name: "", email: "", password: "" });
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
      const res = await registerUser(form);
      const { token, ...userData } = res.data;
      if (!token) {
        setErrors({ general: "Registration succeeded, but no JWT token was returned." });
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
        setErrors({ general: "Registration failed" });
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
            <input
              name="password"
              type="password"
              placeholder="Create password"
              value={form.password}
              onChange={handleChange}
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
