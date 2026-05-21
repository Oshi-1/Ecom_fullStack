import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { updateProfile, updateProfileImage } from "../api/authApi";
import PasswordInput from "../components/PasswordInput";
import { useAuth } from "../context/useAuth";
import styles from "../styles/profile.module.css";

const emptyForm = {
  name: "",
  email: "",
  phone: "",
  alternatePhone: "",
  profilePictureUrl: "",
  address: "",
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
};

const getIndianPhoneDigits = (value) => {
  const digits = String(value || "").replace(/\D/g, "");
  return digits.startsWith("91") && digits.length > 10 ? digits.slice(2, 12) : digits.slice(0, 10);
};

const formatIndianPhone = (value) => {
  const digits = getIndianPhoneDigits(value);
  return digits ? `+91${digits}` : "";
};

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState(() => ({
    ...emptyForm,
    name: user?.name || "",
    email: user?.email || "",
    phone: getIndianPhoneDigits(user?.phone || ""),
    alternatePhone: getIndianPhoneDigits(user?.alternatePhone || ""),
    profilePictureUrl: user?.profilePictureUrl || "",
    address: user?.address || "",
  }));
  const [errors, setErrors] = useState({});
  const [status, setStatus] = useState("");
  const [saving, setSaving] = useState(false);
  const [imageFile, setImageFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(user?.profilePictureUrl || "");
  const [imageOpen, setImageOpen] = useState(false);
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

  const handlePhoneChange = (event) => {
    const { name, value } = event.target;
    const digits = value.replace(/\D/g, "").slice(0, 10);
    setForm((current) => ({ ...current, [name]: digits }));
    setErrors((current) => ({ ...current, [name]: "", general: "" }));
    setStatus("");
  };

  const handleImageChange = (event) => {
    const file = event.target.files?.[0];
    event.target.value = "";

    if (!file) return;

    if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
      setErrors((current) => ({ ...current, profileImage: "Only JPG, PNG, and WebP images are allowed" }));
      return;
    }

    if (file.size > 2 * 1024 * 1024) {
      setErrors((current) => ({ ...current, profileImage: "Profile image cannot exceed 2 MB" }));
      return;
    }

    setImageFile(file);
    if (previewUrl.startsWith("blob:")) {
      URL.revokeObjectURL(previewUrl);
    }
    setPreviewUrl(URL.createObjectURL(file));
    setErrors((current) => ({ ...current, profileImage: "", general: "" }));
    setStatus("");
  };

  const validate = () => {
    const nextErrors = {};
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!form.name.trim()) nextErrors.name = "Name is required";
    if (!form.email.trim()) {
      nextErrors.email = "Email is required";
    } else if (!emailPattern.test(form.email.trim())) {
      nextErrors.email = "Email must be valid";
    }
    if (form.phone.trim() && form.phone.trim().length !== 10) {
      nextErrors.phone = "Enter a valid 10 digit phone number";
    }
    if (form.alternatePhone.trim() && form.alternatePhone.trim().length !== 10) {
      nextErrors.alternatePhone = "Enter a valid 10 digit alternate phone number";
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
        phone: formatIndianPhone(form.phone),
        alternatePhone: formatIndianPhone(form.alternatePhone),
        profilePictureUrl: form.profilePictureUrl.trim(),
        address: form.address.trim(),
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
      };
      const profileRes = await updateProfile(payload);
      let nextData = profileRes.data;

      if (nextData.token) {
        localStorage.setItem("token", nextData.token);
      }

      if (imageFile) {
        const imageRes = await updateProfileImage(imageFile);
        nextData = { ...nextData, ...imageRes.data };
      }

      const { token, ...userData } = nextData;
      updateUser(userData, token);
      if (previewUrl.startsWith("blob:")) {
        URL.revokeObjectURL(previewUrl);
      }
      setPreviewUrl(userData.profilePictureUrl || "");
      setForm((current) => ({
        ...current,
        profilePictureUrl: userData.profilePictureUrl || "",
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      }));
      setImageFile(null);
      setStatus("Profile updated");
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
          <button
            type="button"
            className={styles.avatar}
            onClick={() => previewUrl && setImageOpen(true)}
            disabled={!previewUrl}
            aria-label="View profile image"
          >
            {previewUrl ? <img src={previewUrl} alt="" /> : <span>{initials}</span>}
          </button>
          <div className={styles.uploadActions}>
            <label>
              <input type="file" accept="image/png,image/jpeg,image/webp" onChange={handleImageChange} />
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M5 20h14a2 2 0 0 0 2-2v-5h-2v5H5V6h6V4H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2Zm10-4h2V8h3l-4-4-4 4h3v8ZM7 16h10l-3.1-4.1-2.5 3.2-1.7-2.2L7 16Z" />
              </svg>
              Gallery
            </label>
            <label>
              <input
                type="file"
                accept="image/png,image/jpeg,image/webp"
                capture="environment"
                onChange={handleImageChange}
              />
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M9 5 7.2 7H5a3 3 0 0 0-3 3v7a3 3 0 0 0 3 3h14a3 3 0 0 0 3-3v-7a3 3 0 0 0-3-3h-2.2L15 5H9Zm3 13a4.5 4.5 0 1 1 0-9 4.5 4.5 0 0 1 0 9Zm0-2a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z" />
              </svg>
              Camera
            </label>
          </div>
          {errors.profileImage && <small className={styles.imageError}>{errors.profileImage}</small>}
          <h2>{form.name || "Your name"}</h2>
          <p>{form.email || "you@example.com"}</p>
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
                <div className={styles.phoneInput}>
                  <span>+91</span>
                  <input name="phone" value={form.phone} onChange={handlePhoneChange} autoComplete="tel" />
                </div>
                {errors.phone && <small>{errors.phone}</small>}
              </label>

              <label>
                <span>Alternate Phone Number</span>
                <div className={styles.phoneInput}>
                  <span>+91</span>
                  <input name="alternatePhone" value={form.alternatePhone} onChange={handlePhoneChange} autoComplete="tel" />
                </div>
                {errors.alternatePhone && <small>{errors.alternatePhone}</small>}
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

      {imageOpen && (
        <div className={styles.imageModal} onClick={() => setImageOpen(false)} role="presentation">
          <button type="button" onClick={() => setImageOpen(false)} aria-label="Close image preview">
            Close
          </button>
          <img src={previewUrl} alt="Profile preview" />
        </div>
      )}
    </div>
  );
}
