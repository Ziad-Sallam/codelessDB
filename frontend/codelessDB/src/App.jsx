/* eslint-disable react-refresh/only-export-components */
import { useState, createContext } from "react";
import { RouterProvider, createBrowserRouter } from "react-router-dom";
import { routes } from "./routes.jsx";
import { NotificationProvider } from "./components/NotificationContext.jsx";
import { useAuth } from "./components/AuthProvider.jsx";

export const RecoveryContext = createContext();

function App() {
  const [email, setEmail] = useState("");
  const [otp, setOTP] = useState("");

  const { loading } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  const router = createBrowserRouter(routes);

  return (
    <RecoveryContext.Provider value={{ email, setEmail, otp, setOTP }}>
      <NotificationProvider>
        <RouterProvider router={router} />
      </NotificationProvider>
    </RecoveryContext.Provider>
  );
}

export default App;
