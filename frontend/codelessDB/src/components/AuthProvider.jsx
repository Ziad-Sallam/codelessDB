import { createContext, useContext, useEffect, useState } from "react";
import { validateToken } from "../pages/auth/fetch.js";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function checkAuth() {
      const token = localStorage.getItem("authToken");

      // If no token exists, skip validation
      if (!token) {
        setLoading(false);
        return;
      }

      try {
        const data = await validateToken();
        console.log(data);

        if (data) setUser(data);
        else localStorage.removeItem("authToken");
      } catch (err) {
        console.error("Auth validation failed:", err);
        localStorage.removeItem("authToken");
        setUser(null);
      } finally {
        setLoading(false);
      }
    }

    checkAuth();
  }, []);


  return (
    <AuthContext.Provider value={{ user, setUser, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
