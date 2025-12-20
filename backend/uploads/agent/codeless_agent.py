import json
import os
import threading
import time
import mysql.connector
from mysql.connector import Error, OperationalError, InterfaceError
import websocket
import docker
import sys
import datetime
import decimal
import uuid
import base64
from collections.abc import Iterable
from docker.errors import DockerException, NotFound
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
import mysql.connector


def ensure_docker_installed():
    if shutil.which("docker") is not None:
        return True

    print(" Docker is not installed on this system.\n")

    os_name = platform.system()

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


def get_database_info(
    url: str,
    database_id: int,
):
    try:
        req = requests.post(
            f"{url}/database/create-mysql-container",
            data=json.dumps(database_id),
            headers={"Content-Type": "application/json"},
        )
        print("Response status code:", req.status_code)
        print(json.dumps(req.json(), indent=4))

        if req.status_code != 200:
            print("Failed to get container details from backend.")
            print("Please try again later.")
            exit(1)

        print("--------------------------------")
        return req.json()

    except requests.RequestException as e:
        print(f"Request failed: {e}")
        exit(1)


def create_mysql_container(url: str, database_id: int):
    if not ensure_docker_installed():
        print("Please install Docker.")
        sys.exit(1)

    try:
        client = docker.from_env()
        client.ping()
    except DockerException as e:
        print(f"Docker error: {e}")
        sys.exit(1)

    db = get_database_info(url, database_id)
    container_name = db["databaseName"]
    volume_name = f"{container_name}_data"
    password = db["password"]
    image = "mysql:8.0"
    volFound = True
    containerFound = True
    # Ensure volume exists
    try:
        client.volumes.get(volume_name)
    except NotFound:
        volFound = False
        client.volumes.create(volume_name)

    def create_container():
        print("Creating MySQL container...")
        return client.containers.run(
            image=image,
            name=container_name,
            environment={
                "MYSQL_ROOT_PASSWORD": password,
                "MYSQL_DATABASE": container_name,
            },
            ports={"3306/tcp": None},
            volumes={volume_name: {"bind": "/var/lib/mysql", "mode": "rw"}},
            detach=True,
        )

    try:
        container = client.containers.get(container_name)
        container.reload()

        ports = container.attrs["NetworkSettings"]["Ports"]

        if not ports or ports.get("3306/tcp") is None:
            print("Container has no exposed MySQL port → recreating...")
            container.remove(force=True)
            container = create_container()

        else:
            if container.status != "running":
                print("Restarting container...")
                container.restart()
            else:
                print("Container already running")

    except NotFound:
        containerFound = False
        container = create_container()

    time.sleep(2)
    container.reload()

    host_port = container.attrs["NetworkSettings"]["Ports"]["3306/tcp"][0]["HostPort"]
    print(f"MySQL ready on port {host_port}")
    time.sleep(20)
    if not volFound:
        if containerFound:
            print(f"\033[31mVolume '{volume_name}' was missing and has been created.\033[0m")
        connection = mysql.connector.connect(
            host="localhost",
            port=host_port,
            user="root",
            password=password,
            database=db["databaseName"],
        )
        ddl_statements = db.get("ddl", "").split(";")
        cursor = connection.cursor()
        for stmt in ddl_statements:
            stmt = stmt.strip()
            print("Executing DDL:", stmt)
            if stmt:
                cursor.execute(stmt)
        connection.commit()
        print(f"New MySQL container '{container_name}' created.")

    return container, host_port


if __name__ == "__main__":
    argv = sys.argv
    if len(argv) < 3:
        print("Usage: python codeless_agent.py <url> <database_id>")
        sys.exit(1)
    create_mysql_container(argv[1], int(argv[2]))
