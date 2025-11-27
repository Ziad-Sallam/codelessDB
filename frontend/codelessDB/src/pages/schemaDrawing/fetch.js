import axios from "axios";

export async function generateSQLFromBackend(finalJson) {
  const token = localStorage.getItem("authToken");

  return axios.post(
    "http://localhost:8080/generate",
    finalJson, // request body
    {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      withCredentials: true,
    }
  );
}

export const updateDiagram = (id, payload) => {
  return axios.put(`http://localhost:8080/diagrams/update/${id}`, payload);
};