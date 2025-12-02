#!/usr/bin/env python3
"""
MySQL WebSocket Client - Testable Version
Minimal changes to make the code testable while preserving original structure.
"""

import json
import os
import threading
import time
import mysql.connector
from mysql.connector import Error, OperationalError, InterfaceError
import websocket
import docker
from cryptography.fernet import Fernet
import sys
import datetime
import decimal
import uuid
import base64
from collections.abc import Iterable

# ---------------- Globals ----------------
argv = sys.argv
SCRIPT_ID = None
CONFIG_FILE = None
KEY_FILE = None
url = None
fernet = None
config = None
connection = None
cursor = None
ws_global = None
docker_client = None

# ---------------- Initialization ----------------
def init_globals(websocket_url=None, script_id=None):
    """Initialize global variables. Can be called from tests or main."""
    global argv, SCRIPT_ID, CONFIG_FILE, KEY_FILE, url
    
    if websocket_url and script_id:
        url = websocket_url
        SCRIPT_ID = script_id
    else:
        url = argv[1]
        SCRIPT_ID = argv[2]
    
    CONFIG_FILE = f"client_config_{SCRIPT_ID}.json"
    KEY_FILE = f"client_key_{SCRIPT_ID}.key"

# ---------------- Encryption helpers ----------------
def load_key(key_file=None):
    """Load encryption key from file."""
    key_file = key_file or KEY_FILE
    if os.path.exists(key_file):
        return open(key_file, "rb").read()
    else:
        raise FileNotFoundError("Key file not found. Cannot load encryption key.")

def init_fernet(key_file=None):
    """Initialize Fernet cipher."""
    global fernet
    fernet = Fernet(load_key(key_file))

def encrypt_password(password: str) -> str:
    """Encrypt password using Fernet."""
    return fernet.encrypt(password.encode()).decode()

def decrypt_password(token: str) -> str:
    """Decrypt password using Fernet."""
    return fernet.decrypt(token.encode()).decode()

# ---------------- Config file ----------------
def load_config(config_file=None):
    """Load and decrypt configuration."""
    config_file = config_file or CONFIG_FILE
    if os.path.exists(config_file):
        with open(config_file, "r") as f:
            cfg = json.load(f)
            # decrypt password
            cfg["password"] = decrypt_password(cfg["password"])
            return cfg
    else:
        raise FileNotFoundError("Config file not found. Cannot load configuration.")

def init_config(config_file=None):
    """Initialize global config."""
    global config
    config = load_config(config_file)
    return config

def init_docker():
    """Initialize Docker client."""
    global docker_client
    docker_client = docker.from_env()

# -----------------------------
# STOMP Frame Builders
# -----------------------------

def stomp_connect(username):
    """Create STOMP CONNECT frame."""
    return (
        "CONNECT\n"
        "accept-version:1.2\n"
        f"Authorization:{username}\n"
        "\n\x00"
    )

def stomp_subscribe(destination, sid="sub-0"):
    """Create STOMP SUBSCRIBE frame."""
    return (
        f"SUBSCRIBE\n"
        f"destination:{destination}\n"
        f"id:{sid}\n"
        "\n\x00"
    )

def stomp_send(destination, body, content_type="application/json"):
    """Create STOMP SEND frame."""
    return (
        f"SEND\n"
        f"destination:{destination}\n"
        f"content-type:{content_type}\n"
        "\n"
        f"{body}\x00"
    )

# ----------------- Message Parsing & Handling -----------------

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

    return {
        "command": command,
        "headers": headers,
        "body": body,
        "json": body_json
    }

# ---------------- Docker & MySQL ----------------
def ensure_container_running(container_name=None):
    """Ensure Docker container is running."""
    container_name = container_name or config["database"]
    try:
        container = docker_client.containers.get(container_name)
        if container.status != "running":
            print(f"Container '{container_name}' is {container.status}. Starting...")
            container.start()
            time.sleep(10)
            print(f"Container '{container_name}' started.")
    except:
        print(f"Container '{container_name}' not found. Aborting.")
        exit(1)

def connect_to_mysql(host=None, port=None, user=None, password=None, database=None):
    """Connect to MySQL with retry logic."""
    global connection, cursor
    
    host = host or config["host"]
    port = port or config["port"]
    user = user or config["user"]
    password = password or config["password"]
    database = database or config["database"]
    
    backoff = 5
    while True:
        ensure_container_running()
        try:
            connection = mysql.connector.connect(
                host=host,
                port=port,
                user=user,
                password=password,
                database=database
            )
            if connection.is_connected():
                cursor = connection.cursor()
                print("Connected to MySQL!")
                break
        
        
        except Error as e:
            print(f"MySQL connection failed: {e}. Retrying in {backoff} sec...")
            time.sleep(backoff)
    return connection, cursor

def ensure_mysql_connection():
    """Ensure MySQL connection is active."""
    global connection, cursor
    if connection is None or not connection.is_connected():
        print("Lost MySQL connection. Reconnecting...")
        connect_to_mysql()

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
    """Execute any SQL query and return a standardized response."""
    try:
        cursor.execute(query, params or [])

        # SELECT has cursor.description
        if cursor.description is not None:
            rows = cursor.fetchall()
            columns = [desc[0] for desc in cursor.description]
            safe_rows = [
                 [mysql_value_to_json(col) for i, col in enumerate(row)]
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
        return {
            "success": False,
            "message": str(e)
        }

# ---------------- WebSocket ----------------
def on_message(ws, message):
    """Handle incoming WebSocket messages."""
    global cursor, connection
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
            result = execute_sql(cursor, content["content"])
            result["correlationId"] = content["correlationId"]
                
            payload = json.dumps(result) 
            print("Payload to send:", payload)
            ws.send(stomp_send("/app/response", payload))
            if result["type"] != "SELECT":
                connection.commit()

        except (OperationalError, InterfaceError) as e:
            print("MySQL lost connection. Reconnecting...", e)
            connect_to_mysql()
            try:
                result = execute_sql(cursor, content["content"])
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

def on_error(ws, error):
    """Handle WebSocket errors."""
    print("WebSocket error:", error)

def on_close(ws, close_status_code, close_msg):
    """Handle WebSocket closure."""
    global ws_global
    ws_global = None
    print("### WebSocket closed ###", close_status_code, close_msg)

def on_open(ws):
    """Handle WebSocket opening."""
    global ws_global
    ws_global = ws
    print("### WebSocket opened ###")
    ws.send(stomp_connect(config["id"]))
    time.sleep(0.2)
    ws.send(stomp_subscribe("/user/queue/reply", sid=f"reply-{config['id']}"))

def start_websocket():
    """Start WebSocket connection with retry logic."""
    backoff = 1
    while True:
        try:
            print("Connecting to backend:", url)
            ws = websocket.WebSocketApp(
                url,
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

# ---------------- Input sender ----------------
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

def main(websocket_url=None, script_id=None):
    """Main entry point."""
    if len(argv) < 3 and not (websocket_url and script_id):
        print("Usage: python client.py <websocket_url> <unique_id>")
        sys.exit(1)
    
    init_globals(websocket_url, script_id)
    init_fernet()
    init_config()
    init_docker()
    
    threading.Thread(target=input_loop, daemon=True).start()
    connect_to_mysql()
    
    start_websocket()

# ---------------- Main ---------------- #
if __name__ == '__main__':
    main()