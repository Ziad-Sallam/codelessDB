import docker
import time
import json
import requests

import shutil
import sys
import platform
import subprocess
import os

argv = sys.argv
def ensure_docker_installed():
    # Check if `docker` command exists
    if shutil.which("docker") is not None:
        return True

    print(" Docker is not installed on this system.\n")

    os_name = platform.system()

    # Provide installation instructions based on OS
    if os_name == "Windows":
        print("➡ Download Docker Desktop for Windows:")
        print("   https://docs.docker.com/desktop/install/windows-install/")
    elif os_name == "Darwin":  # macOS
        print("➡ Download Docker Desktop for macOS:")
        print("   https://docs.docker.com/desktop/install/mac-install/")
    elif os_name == "Linux":
        print("➡ Install Docker on Linux:")
        print("   https://docs.docker.com/engine/install/")
        print("\nOr run (Ubuntu):")
        print("   sudo apt update")
        print("   sudo apt install docker.io -y")
    else:
        print("Unsupported OS. Please install Docker manually.")

    return False



def create_mysql_container(id: int):
    if not ensure_docker_installed():
        print("\nPlease install Docker and try again.")
        return -1
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

    except :
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
        ports={"3306/tcp": None},
    )

    print("Waiting for MySQL to initialize (15s)...")
    time.sleep(10)

    print(f"MySQL container '{container_name}' is ready.")
    print(f"Container ID: {container.short_id}")

    env = os.environ.copy()
    env["WS_URL"] = data["ws_url"]
    env["CONTAINER_NAME"] = container_name

    subprocess.run(
    [sys.executable, "communicate.py", data["ws_url"], data["container_id"]],
    env=env
    )


    return 0


if __name__ == "__main__":
    if len(argv) < 2:
        print("Usage: python create_container.py <container_id>")
        sys.exit(1)
    
    
    create_mysql_container(int(argv[1]))
