## create_container.py
from pathlib import Path
import docker
import time
import json
import mysql
import requests
import shutil
import sys
import platform
import subprocess
import os
import socket
from docker.errors import DockerException

from cryptography.fernet import Fernet
import mysql.connector

argv = sys.argv
host_ip="localhost"
def select_random_port():
    
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind(('', 0))
    port = s.getsockname()[1]
    s.close()
    return port


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



def create_mysql_container(id: int, url: str = "http://localhost:8080"):
    if not ensure_docker_installed():
        print("\nPlease install Docker and try again.")
        return -1
    client = 0
    try:
        client = docker.from_env()
        client.ping()  
    except DockerException as e:
        print("Failed to connect to Docker daemon.")
        print("Make sure Docker Desktop is installed and running.")
        print(f"Error: {e}")
        sys.exit(1)

    # Request backend for names
    req = 0
    try:

        req = requests.post(
            f"{url}/database/create-mysql-container",
            data=json.dumps(id),
            headers={"Content-Type": "application/json"}
        )
        print("Response status code:", req.status_code)
        print("Response content:", req.content)
        if req.status_code != 200:
            print("Failed to get container details from backend.")
            print("Response:", req.text)
            print("Please Try again later.")
            print("Exiting...")
            return -1
    except Exception:
        print("500 Server Error :( \n Try again Later")
        exit(-1)
    print("--------------------------------")
    data = req.json()

    print("Received container data:-----------------")
    print(data)
    container_name = data["databaseName"]
    volume_name = f"{container_name}_data"
    database = data["databaseName"]
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
        p = Path(f"client_config_{data['containerId']}.json")
        p2 = Path(f"client_key_{data['containerId']}.key")

        if not p.exists() or not p2.exists:
            host_port = container.attrs['NetworkSettings']['Ports']["3306/tcp"][0]["HostPort"]
            write_db_data(data, "localhost", host_port)


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
        ports={"3306/tcp": select_random_port()},
    )

    print("Waiting for MySQL to initialize (15s)...")
    time.sleep(15)

    container.reload()  
    host_port = container.attrs['NetworkSettings']['Ports']["3306/tcp"][0]["HostPort"]
    host_ip = "localhost"
    

    print("MySQL is exposed on host:", host_ip)
    print("MySQL is exposed on port:", host_port)
    time.sleep(20)
    try:
        connection = mysql.connector.connect(
            host=host_ip,
            port=host_port,
            user="root",
            password=password,
            database=database
        )
    except Exception as e:
        print(f"Failed to connect to MySQL: {e}")
        return -1
    

    print(f"MySQL container '{container_name}' is ready.")
    print(f"Container ID: {container.short_id}")

    env = os.environ.copy()
    env["WS_URL"] = data["wsUrl"]

    write_db_data(data, host_ip, host_port)
    
    print("Executing DDL statements...")
    print(data.get("ddl", ""))

    ddl_statements = data.get("ddl", "").split(";")
    cursor = connection.cursor()
    for stmt in ddl_statements:
        stmt = stmt.strip()
        if stmt:
            cursor.execute(stmt)
    connection.commit()
    cursor.close()
    connection.close()

    print("Databse Created Correctly !")
    print(f'run:    .\communicate.exe \"{data["wsUrl"]}\" {data["containerId"]}')

    return 0

def write_db_data(data, host_ip, host_port):
    SCRIPT_ID = data["containerId"]
    CONFIG_FILE = f"client_config_{SCRIPT_ID}.json"
    KEY_FILE = f"client_key_{SCRIPT_ID}.key"


    key = Fernet.generate_key()
    with open(KEY_FILE, "wb") as f:
        f.write(key)
    
    password_encrypted = Fernet(key).encrypt(data["password"].encode()).decode()

    config = {
            "id" : data["containerId"],
            "host": host_ip,
            "port": host_port,
            "user": "root",
            "password": password_encrypted,
            "database": data["databaseName"]
    }
    with open(CONFIG_FILE, "w") as f:
        json.dump(config, f, indent=4)


def main():
    if len(sys.argv) < 3:
        print("Usage: python create_container.py <url> <container_id>")
        sys.exit(1)

    try:
        int(sys.argv[2])
    except ValueError:
        print("Usage: python create_container.py <url> {}")
        sys.exit(1)

    print("Creating MySQL container with ID:", sys.argv[2])
    print("Using URL:", sys.argv[1])
    create_mysql_container(int(sys.argv[2]), sys.argv[1])

if __name__ == "__main__":
    main()
