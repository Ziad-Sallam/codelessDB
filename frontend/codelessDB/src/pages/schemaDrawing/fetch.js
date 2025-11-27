import axios from "axios";
const token = localStorage.getItem("authToken");

const API_URL = import.meta.env.VITE_BACKEND_URL || "";

export async function fetchDiagram(diagramId) {
	const response = await fetch(`${API_URL}/diagrams/search/${diagramId}`, {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${token}`,
		},
		credentials: "include",
	});

	if (!response.ok) {
		throw new Error(response.json().message)
	}

	return await response.json();
}

export async function generateSQLFromBackend(finalJson) {
  return axios.post(
    `${API_URL}/generate`,
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
  return axios.put(`${API_URL}/diagrams/update/${id}`, payload, {
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    withCredentials: true,
  });
};
