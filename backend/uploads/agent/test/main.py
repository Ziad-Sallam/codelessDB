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
    
    database_name: str
    message: str
    password: str
    ws_url: Optional[str] = None
    container_id: Optional[str] = None
    ddl: Optional[str] = None


# Sample data
data = {
    1: {
        "name": "mysql1",
        "database_name": "db1",
        
        "password": "password1",
        "image": "mysql:8.0",
        "container_name": "mysql1",
        "ws_url": "ws://localhost:8080/agent-ws",
        "container_id": "1",
        "ddl": '''CREATE TABLE users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(150) NOT NULL UNIQUE,
                age INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );'''
    },

    2: {
        "name": "mysql2",
        "database_name": "db2",
        
        "password": "password2",
        "image": "mysql:8.0",
        "container_name": "mysql2",
        "ws_url": "ws://localhost:8080/agent-ws",
        "container_id": "2",
        "ddl": '''CREATE TABLE users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(150) NOT NULL UNIQUE,
                age INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );'''
    },
    3: {
        "name": "mysql3",
        "database_name": "db3",
        
        "password": "password3",
        "image": "mysql:8.0",
        "container_name": "mysql3",
        "ws_url": "ws://localhost:8080/agent-ws",
        "container_id": "3",
        "ddl": '''CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(100) NOT NULL,email VARCHAR(150) NOT NULL UNIQUE,age INT,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);'''
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
        print("Container data for ID", request.id, ":", container_data)

        if not container_data:
            raise HTTPException(status_code=404, detail=f"Container with ID {request.id} not found")

        # Extract values from dictionary (using key access, not attribute access)
        container_name = container_data["name"]
        
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
            
            database_name=database,
            password=password,
            message=f"MySQL container '{container_name}' creation initiated",
            ws_url= container_data["ws_url"],
            container_id= container_data["container_id"],
            ddl= container_data["ddl"]
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error creating container: {str(e)}")


@app.get("/")
async def root():
    return {"message": "MySQL Container Management API"}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)