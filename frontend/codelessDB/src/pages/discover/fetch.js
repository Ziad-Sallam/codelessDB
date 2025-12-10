const API_URL = import.meta.env.VITE_BACKEND_URL || "";

export async function fetchPublicDiagrams(pageNumber = 0, pageSize = 12) {
	const params = new URLSearchParams();
	params.append("pageNumber", pageNumber);
	params.append("pageSize", pageSize);


	const response = await fetch(`${API_URL}/publicDiagrams/search/get?${params.toString()}`, {
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

export async function searchDiagrams(pageNumber = 0, pageSize = 12,
	name = null, start = "1970-01-01", end = "2100-12-31") {
	const params = new URLSearchParams();
	params.append("pageNumber", pageNumber);
	params.append("pageSize", pageSize);

	const body = { name, start, end };

	const response = await fetch(`${API_URL}/diagrams/search?${params.toString()}`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
		body: JSON.stringify(body),

	});

	if (!response.ok) {
		throw new Error(response.json().message)
	}

	return await response.json();
}