const API_URL = import.meta.env.VITE_BACKEND_URL || "";

export async function getPublicDiagram(diagramId) {
  const response = await fetch(`${API_URL}/publicDiagrams/view/${diagramId}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to fetch public diagram");
  }

  return await response.json();
}

export async function forkPublicDiagram(diagramId) {
  const response = await fetch(`${API_URL}/publicDiagrams/fork/${diagramId}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to fork diagram");
  }

  return await response.text();
}

export async function starPublicDiagram(diagramId) {
  const response = await fetch(`${API_URL}/publicDiagrams/star/${diagramId}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to star diagram");
  }

  return await response.text();
}

export async function unstarPublicDiagram(diagramId) {
  const response = await fetch(`${API_URL}/publicDiagrams/unstar/${diagramId}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to unstar diagram");
  }

  return await response.text();
}
