const API_URL = import.meta.env.VITE_BACKEND_URL;

export async function uploadImageToDrive(fileName = "uploaded-image.png") {
	// Load local asset from /assets
	const response = await fetch("/assets/sample.png");
	const blob = await response.blob();

	const form = new FormData();
	form.append("metadata", new Blob([JSON.stringify({
		name: fileName,
		mimeType: blob.type,
	})], { type: "application/json" }));
	form.append("file", blob);

	// Upload to Google Drive
	const uploadResponse = await fetch(
		"https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart",
		{
			method: "POST",
			headers: {
				Authorization: `Bearer ${accessToken}`,
			},
			body: form,
		}
	);

	const data = await uploadResponse.json();
	return data;
}

export async function fetchDiagrams(pageNumber = 0, pageSize = 10) {
	const response = await fetch(`${API_URL}/diagrams/get?pageNumber=${pageNumber}&pageSize=${pageSize}`, {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": `Bearer ${localStorage.getItem("authToken")}`,
		},
		credentials: "include",
	});

	if (!response.ok) {
		throw new Error("Failed to fetch diagrams");
	}

	return await response.json();
}

export async function createDiagram() {
	const defaultDiagram = {
		"schemaName": "TestDB",
		"entities": [
			{
				"name": "Employee",
				"attributes": [
					{
						"name": "id",
						"dataType": { "name": "INT" },
						"indexed": true,
						"autoIncrement": true,
						"constraints": [
							{ "type": "PRIMARY_KEY" },
							{ "type": "NOT_NULL" }
						]
					},
					{
						"name": "name",
						"dataType": { "name": "VARCHAR", "length": 100 },
						"indexed": false,
						"constraints": [
							{ "type": "NOT_NULL" }
						]
					},
					{
						"name": "salary",
						"dataType": { "name": "DECIMAL", "precision": 10, "scale": 2 },
						"indexed": false,
						"constraints": [
							{ "type": "CHECK", "expression": "salary > 0" },
							{ "type": "DEFAULT", "defaultValue": "1000" }
						]
					},
					{
						"name": "department_id",
						"dataType": { "name": "INT" },
						"indexed": false,
						"constraints": [
							{
								"type": "FOREIGN_KEY",
								"referencedTable": "Department",
								"referencedColumn": "id",
								"onDelete": "CASCADE",
								"onUpdate": "NO_ACTION"
							}
						]
					},
					{
						"name": "bonus",
						"dataType": { "name": "FLOAT" },
						"indexed": false,
						"constraints": [
							{ "type": "CHECK", "expression": "bonus >= 0" }
						]
					}
				]
			},
			{
				"name": "Department",
				"attributes": [
					{
						"name": "id",
						"dataType": { "name": "INT" },
						"indexed": true,
						"autoIncrement": true,
						"constraints": [
							{ "type": "PRIMARY_KEY" }
						]
					},
					{
						"name": "name",
						"dataType": { "name": "VARCHAR", "length": 100 },
						"indexed": false,
						"constraints": [
							{ "type": "NOT_NULL" },
							{ "type": "UNIQUE" }
						]
					}
				]
			},
			{
				"name": "Project",
				"attributes": [
					{
						"name": "id",
						"dataType": { "name": "INT" },
						"indexed": true,
						"autoIncrement": true,
						"constraints": [
							{ "type": "PRIMARY_KEY" }
						]
					},
					{
						"name": "name",
						"dataType": { "name": "VARCHAR", "length": 200 },
						"indexed": false,
						"constraints": [
							{ "type": "NOT_NULL" }
						]
					},
					{
						"name": "status",
						"dataType": { "name": "ENUM", "values": ["active", "inactive", "completed"] },
						"indexed": false,
						"constraints": [
							{ "type": "DEFAULT", "defaultValue": "'active'" }
						]
					},
					{
						"name": "tags",
						"dataType": { "name": "SET", "values": ["urgent", "internal", "external"] },
						"indexed": false,
						"constraints": []
					}
				]
			}
		]
	};

	const name = "Untitled Diagram"
	const jsonContent = JSON.stringify(defaultDiagram)
	const thumbnail = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
	// const url = await uploadImage(file);
	// return { id: "d9", name: "Onboarding", createdAt: "2025-07-01", modifiedAt: "2025-09-10", thumbnail: url };

	const body = { jsonContent, name, thumbnail }
	console.log(body)
	
	const response = await fetch(`${API_URL}/diagrams/create`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			"Authorization": `Bearer ${localStorage.getItem("authToken")}`,
		},
		body: JSON.stringify(body),
		credentials: "include",
	});

	if (!response.ok) {
		throw new Error("Failed to create diagram");
	}
	return await { ...body, ...response.json() };
}
