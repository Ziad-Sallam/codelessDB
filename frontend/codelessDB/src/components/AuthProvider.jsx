import { createContext, useContext, useEffect, useState } from "react";
const API_URL = import.meta.env.VITE_BACKEND_URL || "";
const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true); // block rendering until done

  useEffect(() => {
    const token = localStorage.getItem("authToken");
    if (!token) {
      console.log("token is null")
      setLoading(false);
      return;
    }

    fetch(`${API_URL}/user/ay7aga`, {
      headers: { "Authorization": `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error("invalid");
        return res.json();
      })
      .then(data => {
        setUser(data);
      })
      .catch(() => {
        // localStorage.removeItem("authToken");
        setUser(null);
      })
      .finally(() => setLoading(false));
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
