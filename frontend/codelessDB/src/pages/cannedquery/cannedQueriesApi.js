import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

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
export const cannedQueriesApi = {
  getAllQueries: async (databaseId) => {
    const response = await api.get(`/canned-queries/database/${databaseId}`);
    return response.data;
  },
  getQueryById: async (queryId, databaseId) => {
    const response = await api.get(`/canned-queries/${queryId}/database/${databaseId}`);
    return response.data;
  },
  createQuery: async (queryData) => {
    const response = await api.post('/canned-queries', queryData);
    return response.data;
  },
  updateQuery: async (queryId, queryData) => {
    const response = await api.put(`/canned-queries/${queryId}`, queryData);
    return response.data;
  },
  deleteQuery: async (queryId, databaseId) => {
    const response = await api.delete(`/canned-queries/${queryId}/database/${databaseId}`);
    return response.data;
  },
};
export const databaseApi = {
  getUserDatabases: async () => {
    const response = await axios.get('http://localhost:8080/database/get-user-databases', {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
      },
    });
    return response.data;
  },
  getDatabaseDDL: async (databaseId) => {
    const response = await axios.get('http://localhost:8080/database/get-user-databases', {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
      },
    });
    const database = response.data.databases.find(db => db.databaseId === databaseId);
    return database ? database.databaseddl : null;
  },
  createDatabase: async (databaseData) => {
    const response = await api.post('/database/create', databaseData);
    return response.data;
  },
};
export default api;