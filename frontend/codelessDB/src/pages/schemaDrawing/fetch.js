const API_URL = import.meta.env.VITE_BACKEND_URL || "";

async function handleResponse(response) {
  const contentType = response.headers.get("content-type");
  let data;

  if (contentType && contentType.includes("application/json")) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    // data might be string (from backend) or object (JSON)
    const message = typeof data === "string" ? data : data.message;
    throw new Error(message || "Request failed");
  }

  return data;
}

export async function fetchDiagram(diagramId) {
  const response = await fetch(`${API_URL}/room/${diagramId}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  return handleResponse(response);
}

export async function generateSQLFromBackend(finalJson) {
  const response = await fetch(`${API_URL}/generate`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
    body: JSON.stringify(finalJson),
  });

  return handleResponse(response);
}

export async function updateDiagram(id, payload) {
  const response = await fetch(`${API_URL}/room/update/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
    body: JSON.stringify(payload),
  });

  return handleResponse(response);
}
