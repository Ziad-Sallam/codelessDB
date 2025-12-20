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
from docker.errors import DockerException
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


def select_random_port():

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind(("", 0))
    port = s.getsockname()[1]
    s.close()
    return port


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

import requests
import json

def get_database_info(url: str, database_id: int, ):
    try:
        req = requests.post(
            f"{url}/database/create-mysql-container",
            data=json.dumps(database_id),
            headers={"Content-Type": "application/json"}
        )
        print("Response status code:", req.status_code)
        print(json.dumps(req.json(), indent=4))
        
        if req.status_code != 200:
            print("Failed to get container details from backend.")
            print("Please try again later.")
            return None
        
        print("--------------------------------")
        return req.json()

    except requests.RequestException as e:
        print(f"Request failed: {e}")
        return None



def create_mysql_container(url: str, database_id: int):
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
        print(f"Docker Error: {e}")
        sys.exit(1)
    database_info = get_database_info(url, database_id, )
    


if __name__ == "__main__":
    argv = sys.argv
    if len(argv) < 3:
        print("Usage: python codeless_agent.py <_url> <database_id>")
        sys.exit(1)
    create_mysql_container(argv[1], int(argv[2]))
