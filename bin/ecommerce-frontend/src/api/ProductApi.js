import axiosInstance from "./axiosInstance";

export const getAllProducts = () =>
  axiosInstance.get("/products");

export const getProductById = (id) =>
  axiosInstance.get(`/products/${id}`);

export const searchProducts = (keyword, category) =>
  axiosInstance.get("/products/search", {
    params: { keyword, category },
  });

export const addProduct = (data) =>
  axiosInstance.post("/products", data); 

export const updateProduct = (id, data) =>
  axiosInstance.put(`/products/${id}`, data);

export const deleteProduct = (id) =>
  axiosInstance.delete(`/products/${id}`);
