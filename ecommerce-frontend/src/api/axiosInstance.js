import axios from "axios";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8081/api",
});

// Attach JWT to every request automatically
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle expired/invalid token globally
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const requestUrl = error.config?.url || "";
    const isLoginAttempt = requestUrl.includes("/auth/login");

    if (error.response?.status === 401 && !isLoginAttempt) {
      // Token expired or invalid — clear storage and redirect to login
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
