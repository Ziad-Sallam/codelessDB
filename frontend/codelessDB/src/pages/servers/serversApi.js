import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_BACKEND_URL;

// Create a central Axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
  },
});

// Add Authorization header automatically
// api.interceptors.request.use(
//   (config) => {
//     const token = localStorage.getItem('authToken');
//     if (token) {
//       config.headers.Authorization = `Bearer ${token}`;
//     }
//     return config;
//   },
//   (error) => Promise.reject(error)
// );

// API methods
export const serversApi = {
  getUserServers: async () => {
    const { data } = await api.get('/database/get-user-servers');
    return data;
  },

  getUserDatabases: async () => {
    const { data } = await api.get('/database/get-user-databases');
    return data;
  },

  searchUsers: async (query, page = 0, size = 10) => {
    const { data } = await api.get('/user/search', {
      params: { query, page, size }
    });
    return data;
  },

  addUserToDatabase: async (databaseId, userId, role) => {
    await api.post('/database/add-database-to-user', {
      databaseId,
      userId,
      role
    });
  },
  createServer: async (serverName) => {
    const { data } = await api.post('/database/create-server', { serverName });
    return data;
  },
};

export default serversApi;
