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

export async function fetchDiagramMetadata(diagramId) {
  const response = await fetch(`${API_URL}/snapshot/${diagramId}/meta`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  return handleResponse(response);
}

export async function fetchDiagramSnapshot(diagramId) {
  const response = await fetch(`${API_URL}/snapshot/${diagramId}/binary`, {
    method: "GET",
    headers: {
      "Content-Type": "application/octet-stream",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    throw new Error("Failed to fetch snapshot binary");
  }

  return response.arrayBuffer();
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
  const response = await fetch(`${API_URL}/snapshot/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
    body: JSON.stringify(payload),
  });

  return handleResponse(response);
}

export async function updateDiagramMetadata(id, payload) {
  const response = await fetch(`${API_URL}/diagrams/update/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
    body: JSON.stringify(payload),
  });

  return handleResponse(response);
}

