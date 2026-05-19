import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { updateProfile } from "../api/authApi";
import PasswordInput from "../components/PasswordInput";
import { useAuth } from "../context/useAuth";
import styles from "../styles/profile.module.css";

const emptyForm = {
  name: "",
  email: "",
  phone: "",
  profilePictureUrl: "",
  address: "",
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
};

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState(() => ({
    ...emptyForm,
    name: user?.name || "",
    email: user?.email || "",
    phone: user?.phone || "",
    profilePictureUrl: user?.profilePictureUrl || "",
    address: user?.address || "",
  }));
  const [errors, setErrors] = useState({});
  const [status, setStatus] = useState("");
  const [saving, setSaving] = useState(false);
  const [visiblePasswords, setVisiblePasswords] = useState({
    currentPassword: false,
    newPassword: false,
    confirmPassword: false,
  });

  const initials = useMemo(() => {
    const parts = form.name.trim().split(/\s+/).filter(Boolean);
    if (!parts.length) return "U";
    return parts.slice(0, 2).map((part) => part[0].toUpperCase()).join("");
  }, [form.name]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    setErrors((current) => ({ ...current, [name]: "", general: "" }));
    setStatus("");
  };

  const validate = () => {
    const nextErrors = {};
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const phonePattern = /^[0-9+\-()\s]{7,20}$/;

    if (!form.name.trim()) nextErrors.name = "Name is required";
    if (!form.email.trim()) {
      nextErrors.email = "Email is required";
    } else if (!emailPattern.test(form.email.trim())) {
      nextErrors.email = "Email must be valid";
    }
    if (form.phone.trim() && !phonePattern.test(form.phone.trim())) {
      nextErrors.phone = "Phone number must be valid";
    }
    if (form.profilePictureUrl.trim() && !/^https?:\/\/.+/i.test(form.profilePictureUrl.trim())) {
      nextErrors.profilePictureUrl = "Use an http or https image URL";
    }
    if (form.address.length > 600) {
      nextErrors.address = "Address cannot exceed 600 characters";
    }
    if (form.newPassword && form.newPassword.length < 6) {
      nextErrors.newPassword = "Password must be at least 6 characters";
    }
    if (form.newPassword && !form.currentPassword) {
      nextErrors.currentPassword = "Current password is required";
    }
    if (form.newPassword !== form.confirmPassword) {
      nextErrors.confirmPassword = "Passwords do not match";
    }

    return nextErrors;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const nextErrors = validate();

    if (Object.keys(nextErrors).length) {
      setErrors(nextErrors);
      return;
    }

    setSaving(true);
    setErrors({});
    setStatus("");

    try {
      const payload = {
        name: form.name.trim(),
        email: form.email.trim(),
        phone: form.phone.trim(),
        profilePictureUrl: form.profilePictureUrl.trim(),
        address: form.address.trim(),
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
      };
      const res = await updateProfile(payload);
      const { token, ...userData } = res.data;
      updateUser(userData, token);
      setForm((current) => ({
        ...current,
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      }));
      setStatus("Profile saved");
    } catch (err) {
      const data = err.response?.data;
      if (data?.error) {
        setErrors({ general: data.error });
      } else if (data && typeof data === "object") {
        setErrors(data);
      } else {
        setErrors({ general: "Profile could not be saved" });
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className={styles.page}>
      <header className={styles.topbar}>
        <button type="button" onClick={() => navigate("/dashboard")}>Back</button>
        <div>
          <span>Account</span>
          <h1>Profile</h1>
        </div>
      </header>

      <main className={styles.shell}>
        <aside className={styles.summary}>
          <div className={styles.avatar}>
            {form.profilePictureUrl ? <img src={form.profilePictureUrl} alt="" /> : <span>{initials}</span>}
          </div>
          <h2>{form.name || "Your name"}</h2>
          <p>{form.email || "you@example.com"}</p>
          {form.phone && <strong>{form.phone}</strong>}
          {form.address && <small>{form.address}</small>}
        </aside>

        <section className={styles.panel}>
          {errors.general && <p className={styles.errorBanner}>{errors.general}</p>}
          {status && <p className={styles.successBanner}>{status}</p>}

          <form onSubmit={handleSubmit} noValidate>
            <div className={styles.grid}>
              <label>
                <span>Full Name</span>
                <input name="name" value={form.name} onChange={handleChange} autoComplete="name" />
                {errors.name && <small>{errors.name}</small>}
              </label>

              <label>
                <span>Email</span>
                <input name="email" type="email" value={form.email} onChange={handleChange} autoComplete="email" />
                {errors.email && <small>{errors.email}</small>}
              </label>

              <label>
                <span>Phone Number</span>
                <input name="phone" value={form.phone} onChange={handleChange} autoComplete="tel" />
                {errors.phone && <small>{errors.phone}</small>}
              </label>

              <label>
                <span>Profile Picture URL</span>
                <input
                  name="profilePictureUrl"
                  value={form.profilePictureUrl}
                  onChange={handleChange}
                  placeholder="https://example.com/photo.jpg"
                />
                {errors.profilePictureUrl && <small>{errors.profilePictureUrl}</small>}
              </label>
            </div>

            <label>
              <span>Address</span>
              <textarea name="address" value={form.address} onChange={handleChange} rows="4" />
              {errors.address && <small>{errors.address}</small>}
            </label>

            <div className={styles.passwordBlock}>
              <label>
                <span>Current Password</span>
                <PasswordInput
                  name="currentPassword"
                  value={form.currentPassword}
                  onChange={handleChange}
                  autoComplete="current-password"
                  visible={visiblePasswords.currentPassword}
                  onToggle={() =>
                    setVisiblePasswords((current) => ({
                      ...current,
                      currentPassword: !current.currentPassword,
                    }))
                  }
                  className={styles.passwordInput}
                  buttonClassName={styles.passwordToggle}
                />
                {errors.currentPassword && <small>{errors.currentPassword}</small>}
              </label>

              <label>
                <span>New Password</span>
                <PasswordInput
                  name="newPassword"
                  value={form.newPassword}
                  onChange={handleChange}
                  autoComplete="new-password"
                  visible={visiblePasswords.newPassword}
                  onToggle={() =>
                    setVisiblePasswords((current) => ({ ...current, newPassword: !current.newPassword }))
                  }
                  className={styles.passwordInput}
                  buttonClassName={styles.passwordToggle}
                />
                {errors.newPassword && <small>{errors.newPassword}</small>}
              </label>

              <label>
                <span>Confirm Password</span>
                <PasswordInput
                  name="confirmPassword"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  autoComplete="new-password"
                  visible={visiblePasswords.confirmPassword}
                  onToggle={() =>
                    setVisiblePasswords((current) => ({
                      ...current,
                      confirmPassword: !current.confirmPassword,
                    }))
                  }
                  className={styles.passwordInput}
                  buttonClassName={styles.passwordToggle}
                />
                {errors.confirmPassword && <small>{errors.confirmPassword}</small>}
              </label>
            </div>

            <div className={styles.actions}>
              <button type="button" className={styles.secondaryBtn} onClick={() => navigate("/dashboard")}>
                Cancel
              </button>
              <button type="submit" disabled={saving}>
                {saving ? "Saving..." : "Save Profile"}
              </button>
            </div>
          </form>
        </section>
      </main>
    </div>
  );
}
