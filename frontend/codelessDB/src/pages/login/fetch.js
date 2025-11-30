// src/services/authService.js
import axios from "axios";

/**
 * Central axios instance — change baseURL to your backend.
 * Add withCredentials: true if your backend requires cookies.
 */
const api = axios.create({
  baseURL: "http://localhost:8080",
  timeout: 8000,
  headers: {
    "Content-Type": "application/json",
  },
});

/**
 * Helper to normalize error messages from axios / network.
 */
export function parseApiError(error) {
  // server responded with something
  if (error.response) {
    const data = error.response.data;
    // if server returns an object with message property
    if (data?.message) return String(data.message);
    // if server returns a string body
    if (typeof data === "string") return data;
    // fallback to status text
    return error.response.statusText || "Server error";
  }

  // request made but no response / network error
  if (error.request) return "No response from server. Please try again later.";

  // something else happened
  return error.message || "An unknown error occurred.";
}

/**
 * Log in user. Backend may return either a token string or an object: { token: "..." }.
 * Returns the raw response.data.
 */
export async function login(email, password) {
  const response = await api.post("/user/login", { email, password });
  // backend might return token directly or object containing token
  return response.data;
}

/**
 * Check if user exists / request token for password reset.
 * Endpoint: POST /user/login/forgot-password/{email}
 * Returns token on success (as backend currently does).
 */
export async function requestPasswordReset(email) {
  const response = await api.post(`/user/login/forgot-password/${encodeURIComponent(email)}`);
  return response.data;
}

/**
 * Send an OTP (used for both signup and reset flows in your code).
 * Endpoint: POST /user/signup/send-otp/{email}
 * Returns otp (or whatever the backend returns).
 */
export async function sendOtp(email) {
  const response = await api.post(`/user/signup/send-otp/${encodeURIComponent(email)}`);
  return response.data;
}

/**
 * If you want, export the axios instance for other uses:
 */
export { api as axiosInstance };
