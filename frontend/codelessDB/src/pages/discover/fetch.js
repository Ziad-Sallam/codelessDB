const API_URL = import.meta.env.VITE_BACKEND_URL || "";

export async function fetchPublicDiagrams(pageNumber = 0, pageSize = 12, searchPrompt = null, hashtags = []) {
	const params = new URLSearchParams();
	params.append("pageNumber", pageNumber);
	params.append("pageSize", pageSize);

	const body = { searchPrompt, hashtags };

	console.log(body)

	const response = await fetch(`${API_URL}/publicDiagrams/searchDiagrams?${params.toString()}`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
		body: JSON.stringify(body),
	});

	if (!response.ok) {
		const errorData = await response.json();
		throw new Error(errorData.message || "Failed to fetch public diagrams");
	}

	return await response.json();
}

export async function fetchPublicUsers(pageNumber = 0, pageSize = 12, searchPrompt = null, hashtags = []) {
	const params = new URLSearchParams();
	params.append("pageNumber", pageNumber);
	params.append("pageSize", pageSize);

	const body = { searchPrompt, hashtags };

	const response = await fetch(`${API_URL}/publicDiagrams/searchUsers?${params.toString()}`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
		body: JSON.stringify(body),
	});

	if (!response.ok) {
		const errorData = await response.json();
		throw new Error(errorData.message || "Failed to fetch public diagrams");
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

export async function fetchHashtags() {
	const response = await fetch(`${API_URL}/publicDiagrams/hashtags`, {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
	});


	if (!response.ok) {
		throw new Error(response.json().message)
	}

	return await response.json();
}

export async function searchDiagrams(pageNumber = 0, pageSize = 12, searchPrompt = null, hashtags = []) {
	const diagrams = await fetchPublicDiagrams(pageNumber, pageSize, searchPrompt, hashtags);
	const users = await fetchPublicUsers(pageNumber, pageSize, searchPrompt, hashtags);
	return { diagrams, users };
}