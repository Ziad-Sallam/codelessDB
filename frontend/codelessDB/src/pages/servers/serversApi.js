import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/database';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const serversApi = {
    getUserServers: async () => {
        const response = await api.get('/get-user-servers');
        return response.data;
    },
    getUserDatabases: async () => {
        const response = await api.get('/get-user-databases');
        return response.data;
    },
};

export default serversApi;
