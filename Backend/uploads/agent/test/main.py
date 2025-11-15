from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional
import json

app = FastAPI(title="MySQL Container API", version="1.0.0")


# Pydantic model for request
class ContainerRequest(BaseModel):
    id: int


# Pydantic model for response
class ContainerResponse(BaseModel):
    status: str
    container_name: str
    volume_name: str
    database_name: str
    message: str
    password: str


# Sample data
data = {
    1: {
        "name": "mysql1",
        "database_name": "db1",
        "volume_name": "mysql1_data",
        "password": "password1",
        "image": "mysql:8.0",
        "container_name": "mysql1",
    },
    2: {
        "name": "mysql2",
        "database_name": "db2",
        "volume_name": "mysql2_data",
        "password": "password2",
        "image": "mysql:8.0",
        "container_name": "mysql2",
    },
}


@app.post("/create-mysql-container", response_model=ContainerResponse)
async def create_mysql_container(request: ContainerRequest):
    """
    Create a MySQL container with the specified parameters
    """
    try:
        # Get the container data by ID
        container_data = data.get(request.id)

        if not container_data:
            raise HTTPException(status_code=404, detail=f"Container with ID {request.id} not found")

        # Extract values from dictionary (using key access, not attribute access)
        container_name = container_data["name"]
        volume_name = container_data["volume_name"]
        database = container_data["database_name"]
        password = container_data["password"]
        image = container_data["image"]

        # Print the received data
        print("Received container creation request:")
        print(json.dumps(container_data, indent=4))

        # Here you would typically add your container creation logic
        # For example using Docker SDK, subprocess, etc.

        return ContainerResponse(
            status="success",
            container_name=container_name,
            volume_name=volume_name,
            database_name=database,
            password=password,
            message=f"MySQL container '{container_name}' creation initiated"
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error creating container: {str(e)}")


@app.get("/")
async def root():
    return {"message": "MySQL Container Management API"}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)