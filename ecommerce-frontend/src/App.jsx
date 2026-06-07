import { BrowserRouter } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { NotificationProvider } from "./context/NotificationContext";
import AppRoutes from "./routes/AppRoutes";
import "./styles/global.css";

export default function App() {
  return (
    <BrowserRouter>
      <NotificationProvider>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </NotificationProvider>
    </BrowserRouter>
  );
}
