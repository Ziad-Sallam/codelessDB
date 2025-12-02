import axios from "axios";

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

const apiClient = axios.create({
  baseURL: BACKEND_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("authToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem("authToken");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export const updateUserField = async (field, value) => {
  const response = await apiClient.put("/user/update", { [field]: value });
  return response.data;
};

export const resetPassword = async (email) => {
  const response = await apiClient.post(`/user/login/forgot-password/${encodeURIComponent(email)}`);
  return response.data;
};
