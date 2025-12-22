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
    const message = typeof data === "string" ? data : data.message;
    throw new Error(message || "Request failed");
  }

  return data;
}

export async function optimizeSQLWithGemini(originalSQL) {
  const response = await fetch(`${API_URL}/api/optimize-sql`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
    body: JSON.stringify({ sqlCode: originalSQL }),
  });

  const data = await handleResponse(response);
  
  return data;
}


export async function updateDDL(id, ddl) {
  const response = await fetch(`${API_URL}/diagrams/update-ddl/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
    body: JSON.stringify({ ddl }),
  });

  return handleResponse(response);
}