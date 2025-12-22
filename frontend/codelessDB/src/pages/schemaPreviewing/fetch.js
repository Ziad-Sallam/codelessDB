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

// Comments API
export async function getComments(diagramId) {
  const response = await fetch(`${API_URL}/publicDiagrams/${diagramId}/comments`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to fetch comments");
  }

  return await response.json();
}

export async function addComment(diagramId, content, parentId = null) {
  const response = await fetch(`${API_URL}/publicDiagrams/${diagramId}/comments`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
    body: JSON.stringify({ content, parentId }),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to add comment");
  }

  return await response.json();
}

export async function updateComment(commentId, content) {
  const response = await fetch(`${API_URL}/publicDiagrams/comments/${commentId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
    body: JSON.stringify({ content }),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to update comment");
  }

  return await response.json();
}

export async function deleteComment(commentId) {
  const response = await fetch(`${API_URL}/publicDiagrams/comments/${commentId}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to delete comment");
  }

  return true;
}

export async function reactToComment(commentId, type) {
  const response = await fetch(`${API_URL}/publicDiagrams/comments/${commentId}/react?type=${type}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Failed to react to comment");
  }

  return await response.json();
}
