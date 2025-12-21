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


# -----------------------------
# Container Creation Logic
# -----------------------------
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
    connection = None
    cursor = None
    if not volFound:
        if containerFound:
            print(
                f"\033[31mVolume '{volume_name}' was missing and has been created.\033[0m"
            )
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

    return container, db, host_port, connection, cursor


# -----------------------------
# SQL Execution and Data Conversion Logic
# -----------------------------


def mysql_value_to_json(value):
    """Convert any MySQL value to JSON-serializable form."""
    if value is None:
        return None

    if isinstance(value, datetime.datetime):
        return value.strftime("%Y-%m-%d %H:%M:%S")

    if isinstance(value, datetime.date):
        return value.strftime("%Y-%m-%d")

    if isinstance(value, datetime.time):
        return value.strftime("%H:%M:%S")

    if isinstance(value, decimal.Decimal):
        return float(value)

    if isinstance(value, uuid.UUID):
        return str(value)

    if isinstance(value, (bytes, bytearray)):
        return base64.b64encode(value).decode("utf-8")

    if isinstance(value, Iterable) and not isinstance(value, (str, bytes, dict)):
        return [mysql_value_to_json(v) for v in value]

    return str(value)


def execute_sql(cursor, query, params=None):
    if cursor is None:
        return {"success": False, "message": "Cursor is not initialized"}

    try:
        cursor.execute(query, params or [])

        if cursor.description is not None:
            rows = cursor.fetchall()
            columns = [desc[0] for desc in cursor.description]
            safe_rows = [
                [mysql_value_to_json(col) for col in row]
                for row in rows
            ]
            return {
                "success": True,
                "type": "SELECT",
                "columns": columns,
                "rows": safe_rows,
                "rowCount": len(rows),
            }

        return {
            "success": True,
            "type": "NON-SELECT",
            "rowCount": cursor.rowcount,
            "message": "Query executed successfully.",
        }

    except Exception as e:
        return {"success": False, "message": str(e)}


def connect_to_mysql(host=None, port=None, user=None, password=None, database=None):
    global connection_global, cursor_global, host_port_global, data

    host = host or "localhost"
    port = port or host_port_global
    user = user or "root"
    password = password or data["password"]
    database = database or data["databaseName"]

    backoff = 5
    while True:
        ensure_container_running()
        try:
            connection = mysql.connector.connect(
                host=host,
                port=port,
                user=user,
                password=password,
                database=database,
            )
            cursor = connection.cursor()
            print("Connected to MySQL!")
            return connection, cursor

        except Error as e:
            print(f"MySQL connection failed: {e}. Retrying in {backoff}s")
            time.sleep(backoff)

def ensure_container_running(container_name=None):
    container_name = container_name or data["databaseName"]
    try:
        client = docker.from_env()
        container = client.containers.get(container_name)
        container.reload()

        if container.status != "running":
            print(f"Starting container '{container_name}'...")
            container.start()
            time.sleep(10)

    except NotFound:
        print(f"Container '{container_name}' not found.")
        sys.exit(1)

    except DockerException as e:
        print("Docker error:", e)
        sys.exit(1)


def ensure_mysql_connection():
    global connection_global, cursor_global
    if connection_global is None or not connection_global.is_connected():
        print("Lost MySQL connection. Reconnecting...")
        connection_global, cursor_global = connect_to_mysql()


# -----------------------------
# WebSocket and DDL Execution Logic
# -----------------------------
def stomp_connect(username):
    """Create STOMP CONNECT frame."""
    return "CONNECT\n" "accept-version:1.2\n" f"Authorization:{username}\n" "\n\x00"


def stomp_subscribe(destination, sid="sub-0"):
    """Create STOMP SUBSCRIBE frame."""
    return f"SUBSCRIBE\n" f"destination:{destination}\n" f"id:{sid}\n" "\n\x00"


def stomp_send(destination, body, content_type="application/json"):
    """Create STOMP SEND frame."""
    return (
        f"SEND\n"
        f"destination:{destination}\n"
        f"content-type:{content_type}\n"
        "\n"
        f"{body}\x00"
    )


def parse_stomp_message(frame: str):
    """Parse a STOMP MESSAGE frame and return the body JSON."""
    # Remove trailing null byte if exists
    frame = frame.rstrip("\x00")

    # Split headers and body (first blank line)
    parts = frame.split("\n\n", 1)
    if len(parts) != 2:
        return None  # Invalid frame

    header_block, body = parts

    # Parse headers into dict
    headers = {}
    header_lines = header_block.split("\n")
    # First line is the command (MESSAGE), skip it
    command = header_lines[0].strip()

    for line in header_lines[1:]:
        if ":" in line:
            key, val = line.split(":", 1)
            headers[key.strip()] = val.strip()

    # Try to parse body as JSON
    try:
        body_json = json.loads(body)
    except json.JSONDecodeError:
        body_json = None

    return {"command": command, "headers": headers, "body": body, "json": body_json}

def on_open(ws):
    """Handle WebSocket opening."""
    global ws_global
    ws_global = ws
    print("### WebSocket opened ###")
    ws.send(stomp_connect(data["containerId"]))
    time.sleep(0.2)
    ws.send(stomp_subscribe("/user/queue/reply", sid=f"reply-{data['containerId']}"))


def on_error(ws, error):
    """Handle WebSocket errors."""
    print("WebSocket error:", error)


def on_close(ws, close_status_code, close_msg):
    """Handle WebSocket closure."""
    global ws_global
    ws_global = None
    print("### WebSocket closed ###", close_status_code, close_msg)


def on_message(ws, message):
    """Handle incoming WebSocket messages."""
    global connection_global, cursor_global
    print("------------------------------")
    print("SERVER:", message)
    print("------------------------------")
    ensure_mysql_connection()

    if message.startswith("MESSAGE"):
        message_data = parse_stomp_message(message)
        if message_data and message_data["json"]:
            content = message_data["json"]
            if not content:
                return
        print("Parsed message content:", content["content"])    
        try:
            result = execute_sql(cursor_global, content["content"])
            result["correlationId"] = content["correlationId"]
                
            payload = json.dumps(result) 
            print("Payload to send:", payload)
            ws.send(stomp_send("/app/response", payload))
            if result["type"] != "SELECT":
                connection_global.commit()

        except (OperationalError, InterfaceError) as e:
            print("MySQL lost connection. Reconnecting...", e)
            connection_global, cursor_global = connect_to_mysql()
            try:
                result = execute_sql(cursor_global, content["content"])
                result["correlationId"] = content["correlationId"]
                    
                payload = json.dumps(result) 
                print("Payload to send:", payload)
                ws.send(stomp_send("/app/response", payload))
                if result["type"] != "SELECT":
                    connection.commit()
                
            except Exception as e2:
                print("Failed to execute SQL after reconnect:", e2)
                payload = json.dumps({"result": "Failed to execute SQL after reconnect: " + str(e2), "correlationId": content["correlationId"]})
                ws.send(stomp_send("/app/response", payload))
        except Exception as e:
            print("SQL Error:", e)
            payload = json.dumps({"success": False, "message": str(e), "correlationId": content["correlationId"]})
            ws.send(stomp_send("/app/response", payload))

def start_websocket():
    """Start WebSocket connection with retry logic."""
    backoff = 1
    while True:
        try:
            print("Connecting to backend:", data["wsUrl"])
            ws = websocket.WebSocketApp(
                data["wsUrl"],
                on_open=on_open,
                on_message=on_message,
                on_error=on_error,
                on_close=on_close
            )
            ws.run_forever(ping_interval=25, ping_timeout=20)
        except Exception as e:
            print("WebSocket exception:", e)
        print(f"Reconnecting WebSocket in {backoff} seconds...")
        time.sleep(backoff)
        backoff = min(backoff * 2, 5)

def input_loop():
    """Handle user input."""
    global ws_global
    while True:
        text = input()
        if ws_global:
            try:
                ws_global.send(json.dumps({"client_msg": text}))
            except:
                print("Failed to send message (WebSocket disconnected)")
        else:
            print("WebSocket not connected. Waiting...")


if __name__ == "__main__":
    argv = sys.argv
    if len(argv) < 3:
        print("Usage: python codeless_agent.py <url> <database_id>")
        sys.exit(1)
    container, db, host_port, connection, cursor = create_mysql_container(
        argv[1], int(argv[2])
    )
    global ws_global, connection_global, cursor_global, host_port_global
    ws_global = None
    connection_global = connection
    cursor_global = cursor
    if cursor_global is None and connection_global is not None:
        cursor_global = connection_global.cursor()
    host_port_global = host_port
    data = db
    start_websocket()
