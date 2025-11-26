const API_URL = import.meta.env.VITE_BACKEND_URL || "";

export async function fetchDiagrams(pageNumber = 0, pageSize = 10, { search, dateFrom, dateTo } = {}) {
	const params = new URLSearchParams();
	params.append("pageNumber", pageNumber);
	params.append("pageSize", pageSize);

	if (search) params.append("search", search);
	if (dateFrom) params.append("dateFrom", dateFrom);
	if (dateTo) params.append("dateTo", dateTo);

	const response = await fetch(`${API_URL}/diagrams/get?${params.toString()}`, {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
		credentials: "include",
	});

	if (!response.ok) {
		throw new Error(response.json().message)
	}

	return await response.json();
}

export async function createDiagram() {
	const response = await fetch(`${API_URL}/diagrams/create`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
		body: JSON.stringify({ thumbnail: "" }),
		credentials: "include",
	});

	if (!response.ok) {
		throw new Error(response.json().message)
	}

	return await response.json();
}

export async function renameDiagram(diagramId, newName) {
	const body = { name: newName };
	const response = await fetch(`${API_URL}/diagrams/update/${diagramId}`, {
		method: "PUT",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
		body: JSON.stringify(body),
		credentials: "include",
	});

	if (!response.ok) {
		throw new Error(response.json().message)
	}

	return await response.json();
}

export async function shareDiagram(diagramId, toUserName, role) {
	const body = { toUserName, role };
	const response = await fetch(`${API_URL}/diagrams/share/${diagramId}`, {
		method: "PUT",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
		body: JSON.stringify(body),
		credentials: "include",
	});

	if (!response.ok) {
		throw new Error(response.json().message)
	}

	return await response.json();
}

export async function deleteDiagram(diagramId) {
	const response = await fetch(`${API_URL}/diagrams/delete/${diagramId}`, {
		method: "DELETE",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
		credentials: "include",
	});

	if (!response.ok) {
		throw new Error(response.json().message)
	}

	return true;
}
