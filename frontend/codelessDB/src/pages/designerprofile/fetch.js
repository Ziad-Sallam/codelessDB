const API_URL = import.meta.env.VITE_BACKEND_URL || "";

export async function fetchDesignerProfile(userName) {
  const response = await fetch(`${API_URL}/publicUsers/designerProfile/${userName}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const errorData = await response.json();
    const error = new Error(errorData.message || "Failed to fetch designer profile");
    error.status = response.status;
    throw error;
  }

  return await response.json();
}

export async function fetchPublicDiagrams(userName, pageNumber = 0, pageSize = 10) {
  const response = await fetch(`${API_URL}/publicUsers/publicDiagrams/${userName}?pageNumber=${pageNumber}&pageSize=${pageSize}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || "Failed to fetch public diagrams");
  }

  return await response.json();
}

export async function fetchStarredDiagrams(userName, pageNumber = 0, pageSize = 10) {
  const response = await fetch(`${API_URL}/publicUsers/staredDiagrams/${userName}?pageNumber=${pageNumber}&pageSize=${pageSize}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || "Failed to fetch starred diagrams");
  }

  return await response.json();
}

export async function followUser(userName) {
  const response = await fetch(`${API_URL}/publicUsers/follow/${userName}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || "Failed to follow user");
  }

  return await response.text();
}

export async function unfollowUser(userName) {
  const response = await fetch(`${API_URL}/publicUsers/unfollow/${userName}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || "Failed to unfollow user");
  }

  return await response.text();
}

export async function fetchFollowers(userName, pageNumber = 0, pageSize = 10) {
  const response = await fetch(`${API_URL}/publicUsers/followers/${userName}?pageNumber=${pageNumber}&pageSize=${pageSize}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || "Failed to fetch followers");
  }

  return await response.json();
}

export async function fetchFollowings(userName, pageNumber = 0, pageSize = 10) {
  const response = await fetch(`${API_URL}/publicUsers/followings/${userName}?pageNumber=${pageNumber}&pageSize=${pageSize}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("authToken")}`,
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || "Failed to fetch followings");
  }

  return await response.json();
}
