import { useCallback, useMemo, useState } from "react";
import { NotificationContext } from "./NotificationContextObject";
import styles from "../styles/notification.module.css";

const titles = {
  success: "Success",
  error: "Action needed",
  info: "Notice",
};

export function NotificationProvider({ children }) {
  const [notification, setNotification] = useState(null);

  const showNotification = useCallback((type, message, options = {}) => {
    const nextNotification = {
      id: Date.now(),
      type,
      title: options.title || titles[type] || "Notice",
      message,
    };

    setNotification(nextNotification);

    const duration = options.duration ?? 8000;
    if (duration > 0) {
      window.setTimeout(() => {
        setNotification((current) => (current?.id === nextNotification.id ? null : current));
      }, duration);
    }
  }, []);

  const value = useMemo(() => ({
    showSuccess: (message, options) => showNotification("success", message, options),
    showError: (message, options) => showNotification("error", message, options),
    showInfo: (message, options) => showNotification("info", message, options),
    clearNotification: () => setNotification(null),
  }), [showNotification]);

  return (
    <NotificationContext.Provider value={value}>
      {children}
      {notification && (
        <div
          role="alert"
          data-testid="app-notification"
          className={`${styles.toast} ${styles[notification.type]}`}
        >
          <span className={styles.mark} aria-hidden="true" />
          <div>
            <strong>{notification.title}</strong>
            <p>{notification.message}</p>
          </div>
          <button type="button" aria-label="Close notification" onClick={() => setNotification(null)}>
            x
          </button>
        </div>
      )}
    </NotificationContext.Provider>
  );
}
