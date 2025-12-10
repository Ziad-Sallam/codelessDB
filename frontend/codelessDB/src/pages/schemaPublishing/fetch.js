const API_URL = import.meta.env.VITE_BACKEND_URL || "";

export async function fetchToBePublished(pageNumber = 0, pageSize = 10) {
	const response = await fetch(`${API_URL}/publicDiagrams/getToBePublished?pageNumber=${pageNumber}&pageSize=${pageSize}`, {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
	});

	if (!response.ok) {
		throw new Error(response.json().message)
	}

	const data = await response.json();
	return data;
}

export async function publishSchema(diagramId, shortDescription, detailedDescription, hashTags, cannedQueries) {
	const response = await fetch(`${API_URL}/publicDiagrams/publish`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
		body: JSON.stringify({
			diagramId,
			shortDescription,
			detailedDescription,
			hashTags,
			cannedQueries,
		}),
	});

	if (!response.ok) {
		const error = await response.json();
		throw new Error(error.message);
	}

	return response;
}
