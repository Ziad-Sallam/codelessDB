import axios from "axios";

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";

const api = axios.create({
  baseURL: BACKEND_URL,
  timeout: 8000,
  headers: {
    "Content-Type": "application/json",
  },
});

export function parseApiError(error) {
  if (error.response) {
    const data = error.response.data;
    if (data?.message) return String(data.message);
    if (typeof data === "string") return data;
    return error.response.statusText || "Server error";
  }

  if (error.request) return "No response from server. Please try again later.";
  return error.message || "An unknown error occurred.";
}

export async function login(email, password) {
  const response = await api.post("/user/login", { email, password });
  return response.data;
}

export async function validateSignup(email, username) {
  const response = await api.post("/user/signup/validate", {
    email,
    username,
  });
  return response.data;
}

export async function validateToken() {
  const token = localStorage.getItem("authToken");
  if (!token) return null;

  const response = await api.get("/user/auth", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data;
}

export async function sendOtp(email, username) {
  const query = username ? `?username=${encodeURIComponent(username)}` : "";
  const response = await api.post(`/user/signup/send-otp/${encodeURIComponent(email)}${query}`);
  return response.data;
}

export async function completeSignup(username, email, password, picture = null) {
  const response = await api.post("/user/signup", {
    username,
    email,
    password,
    picture,
  });
  return response.data;
}

export async function requestPasswordReset(email) {
  const response = await api.post(`/user/login/forgot-password/${encodeURIComponent(email)}`);
  return response.data;
}

export async function updatePassword(password, token) {
  const response = await api.put(
    "/user/update",
    { password },
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.data;
}

export function redirectToGoogleAuth(isSignup = true) {
  window.location.href = `${BACKEND_URL}/oauth2/authorization/google`;
}

export { api as axiosInstance };