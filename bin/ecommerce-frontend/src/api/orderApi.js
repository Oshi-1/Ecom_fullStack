import axiosInstance from "./axiosInstance";

export const placeOrder = (checkoutDetails) => axiosInstance.post("/orders", checkoutDetails);

export const getMyOrders = () => axiosInstance.get("/orders/my");
