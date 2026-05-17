import { Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import DashboardPage from "../pages/DashboardPage";
import ProductsPage from "../pages/ProductsPage";
import AddProductPage from "../pages/AddProductPage";
import ProductDetailsPage from "../pages/ProductDetailsPage";
import CartPage from "../pages/CartPage";
import CheckoutPage from "../pages/CheckoutPage";
import OrderSuccessPage from "../pages/OrderSuccessPage";
import UsersPage from "../pages/UsersPage";
import OrderHistoryPage from "../pages/OrderHistoryPage";
import UserOrdersPage from "../pages/UserOrdersPage";
import ProtectedRoute from "../components/ProtectedRoute";
import AdminRoute from "../components/AdminRoute";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/dashboard" element={
        <ProtectedRoute><DashboardPage /></ProtectedRoute>
      }/>
      <Route path="/products" element={
        <ProtectedRoute><ProductsPage /></ProtectedRoute>
      }/>
      <Route path="/products/new" element={
        <ProtectedRoute><AdminRoute><AddProductPage /></AdminRoute></ProtectedRoute>
      }/>
      <Route path="/products/:id/edit" element={
        <ProtectedRoute><AdminRoute><AddProductPage /></AdminRoute></ProtectedRoute>
      }/>
      <Route path="/products/:id" element={
        <ProtectedRoute><ProductDetailsPage /></ProtectedRoute>
      }/>
      <Route path="/cart" element={
        <ProtectedRoute><CartPage /></ProtectedRoute>
      }/>
      <Route path="/checkout" element={
        <ProtectedRoute><CheckoutPage /></ProtectedRoute>
      }/>
      <Route path="/order-success" element={
        <ProtectedRoute><OrderSuccessPage /></ProtectedRoute>
      }/>
      <Route path="/users" element={
        <ProtectedRoute><AdminRoute><UsersPage /></AdminRoute></ProtectedRoute>
      }/>
      <Route path="/orders" element={
        <ProtectedRoute><AdminRoute><OrderHistoryPage /></AdminRoute></ProtectedRoute>
      }/>
      <Route path="/my-orders" element={
        <ProtectedRoute><UserOrdersPage /></ProtectedRoute>
      }/>
    </Routes>
  );
}
