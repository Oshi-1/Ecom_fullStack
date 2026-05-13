import axiosInstance from "./axiosInstance";

export const getCart = () => axiosInstance.get("/cart");

export const addToCart = (productId, quantity = 1) =>
  axiosInstance.post("/cart/items", { productId, quantity });

export const updateCartItem = (cartItemId, quantity) =>
  axiosInstance.put(`/cart/items/${cartItemId}`, { quantity });

export const removeCartItem = (cartItemId) =>
  axiosInstance.delete(`/cart/items/${cartItemId}`);

export const clearCart = () => axiosInstance.delete("/cart");
