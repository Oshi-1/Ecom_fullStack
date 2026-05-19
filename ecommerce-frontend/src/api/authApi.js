import axiosInstance from "./axiosInstance";

export const registerUser = (data) => axiosInstance.post("/auth/register", data);
export const loginUser = (data) => axiosInstance.post("/auth/login", data);
export const forgotPassword = (data) => axiosInstance.post("/auth/forgot-password", data);
export const updateProfile = (data) => axiosInstance.put("/auth/profile", data);
