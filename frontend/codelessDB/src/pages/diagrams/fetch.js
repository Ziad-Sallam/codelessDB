const API_URL = import.meta.env.VITE_BACKEND_URL || "";

export async function fetchDiagrams(pageNumber = 0, pageSize = 12, { search, dateFrom, dateTo } = {}) {
	const params = new URLSearchParams();
	params.append("pageNumber", pageNumber);
	params.append("pageSize", pageSize);

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

export async function getUserInfo() {
	const response = await fetch(`${API_URL}/user/info`, {
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
	const thumbnail = 'https://res.cloudinary.com/dltspdjod/image/upload/v1764182329/new_diagram_vf6icr.jpg';

	const response = await fetch(`${API_URL}/diagrams/create`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			Authorization: `Bearer ${localStorage.getItem("authToken")}`,
		},
		body: JSON.stringify({ thumbnail }),
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
		credentials: "include",
	});

	if (!response.ok) {
		throw new Error(response.json().message)
	}

	return await response.json();
}

export async function shareDiagram(diagramId, toUserName, role, deleteUser = false) {
	const body = { toUserName, role, delete: deleteUser };
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
