import docker
import time
import json
import requests


def create_mysql_container(id: int):
    client = docker.from_env()

    # Request backend for names
    req = requests.post(
        "http://localhost:8000/create-mysql-container",
        data=json.dumps({"id": id})
    )
    data = req.json()

    container_name = data["container_name"]
    volume_name = f"{container_name}_data"
    database = data["database_name"]
    password = data["password"]
    image = "mysql:8.0"

    print(f"Container name: {container_name}")
    print(f"Volume name   : {volume_name}")

    # ---------------------------------------
    # CHECK IF CONTAINER ALREADY EXISTS
    # ---------------------------------------
    try:
        container = client.containers.get(container_name)
        print(f"Container '{container_name}' already exists.")

        if container.status != "running":
            print("Starting container...")
            container.start()
            time.sleep(10)
            print("Container started.")
        else:
            print("Container already running.")

        return 0

    except docker.errors.NotFound:
        print(f"Container '{container_name}' does not exist, creating a new one...")


    existing_volumes = {v.name for v in client.volumes.list()}

    if volume_name not in existing_volumes:
        print(f"Volume '{volume_name}' not found. Creating...")
        client.volumes.create(name=volume_name)
    else:
        print(f"Volume '{volume_name}' already exists.")


    # ---------------------------------------
    # PULL IMAGE
    # ---------------------------------------
    print("Pulling MySQL image if needed...")
    client.images.pull(image)


    # ---------------------------------------
    # CREATE CONTAINER (FIRST TIME)
    # ---------------------------------------
    print("Creating container...")

    container = client.containers.run(
        image=image,
        name=container_name,
        detach=True,
        environment={
            "MYSQL_ROOT_PASSWORD": password,
            "MYSQL_DATABASE": database,
        },
        volumes={
            volume_name: {"bind": "/var/lib/mysql", "mode": "rw"},
        },
        ports={"3306/tcp": 3306},
    )

    print("Waiting for MySQL to initialize (15s)...")
    time.sleep(10)

    print(f"MySQL container '{container_name}' is ready.")
    print(f"Container ID: {container.short_id}")

    return 1


if __name__ == "__main__":
    create_mysql_container(2)
