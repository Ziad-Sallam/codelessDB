### communicate.py

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

argv = sys.argv

if len(argv) < 3:
        print("Usage: python client.py <websocket_url> <unique_id>")
        sys.exit(1)
    
    
SCRIPT_ID = argv[2]
CONFIG_FILE = f"client_config_{SCRIPT_ID}.json"
KEY_FILE = f"client_key_{SCRIPT_ID}.key"
url = argv[1]

# ---------------- Encryption helpers ----------------
def load_key():
    if os.path.exists(KEY_FILE):
        return open(KEY_FILE, "rb").read()
    else:
        raise FileNotFoundError("Key file not found. Cannot load encryption key.")

fernet = Fernet(load_key())

def encrypt_password(password: str) -> str:
    return fernet.encrypt(password.encode()).decode()

def decrypt_password(token: str) -> str:
    return fernet.decrypt(token.encode()).decode()

# ---------------- Config file ----------------
def load_config():
    if os.path.exists(CONFIG_FILE):
        with open(CONFIG_FILE, "r") as f:
            config = json.load(f)
            # decrypt password
            config["password"] = decrypt_password(config["password"])
            return config
    else:
        raise FileNotFoundError("Config file not found. Cannot load configuration.")
        

# ---------------- Globals ----------------
config = load_config()

id = config["id"]
host = config["host"]
port = config["port"]
user = config["user"]
password = config["password"]
database = config["database"]
container_name = config["database"]

connection = None
cursor = None
ws_global = None
docker_client = docker.from_env()

# -----------------------------
# STOMP Frame Builders
# -----------------------------

def stomp_connect(username):
    return (
        "CONNECT\n"
        "accept-version:1.2\n"
        f"Authorization:{username}\n"
        "\n\x00"
    )

def stomp_subscribe(destination, sid="sub-0"):
    return (
        f"SUBSCRIBE\n"
        f"destination:{destination}\n"    #/user/queue/reply
        f"id:{sid}\n"
        "\n\x00"
    )

def stomp_send(destination, body, content_type="application/json"):
    return (
        f"SEND\n"
        f"destination:{destination}\n"
        f"content-type:{content_type}\n"
        "\n"
        f"{body}\x00"
    )


# ----------------- Message Parsing & Handling -----------------

def parse_stomp_message(frame: str):
    """
    Parse a STOMP MESSAGE frame and return the body JSON.
    """

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
def ensure_container_running():
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

def connect_to_mysql():
    global connection, cursor
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

def ensure_mysql_connection():
    global connection, cursor
    if connection is None or not connection.is_connected():
        print("Lost MySQL connection. Reconnecting...")
        connect_to_mysql()

import json
import datetime
import decimal
import uuid
import base64
from collections.abc import Iterable

def mysql_value_to_json(value):
    """
    Convert any MySQL value to JSON-serializable form.
    Mimics MySQL display formatting where possible.
    """
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
    """
    Execute any SQL query and return a standardized response.
    """
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
            result = execute_sql(cursor, content["content"]) # type: ignore
            result["correlationId"] = content["correlationId"]
                
            payload = json.dumps(result) 
            print("Payload to send:", payload)
            ws.send(stomp_send("/app/response", payload))
            if result["type"] != "SELECT":
                connection.commit() # type: ignore

        except (OperationalError, InterfaceError) as e:
            print("MySQL lost connection. Reconnecting...", e)
            connect_to_mysql()
            try:
                result = execute_sql(cursor, content["content"]) # type: ignore
                result["correlationId"] = content["correlationId"]
                    
                payload = json.dumps(result) 
                print("Payload to send:", payload)
                ws.send(stomp_send("/app/response", payload))
                if result["type"] != "SELECT":
                    connection.commit() # type: ignore
                
            except Exception as e2:
                print("Failed to execute SQL after reconnect:", e2)
                payload = json.dumps({"result": "Failed to execute SQL after reconnect: " + str(e2), "correlationId": content["correlationId"]})
                ws.send(stomp_send("/app/response", payload))
        except Exception as e:
            print("SQL Error:", e)
            payload = json.dumps({"success": False, "message": str(e), "correlationId": content["correlationId"]})
            ws.send(stomp_send("/app/response", payload))

def on_error(ws, error):
    print("WebSocket error:", error)

def on_close(ws, close_status_code, close_msg):
    global ws_global
    ws_global = None
    print("### WebSocket closed ###", close_status_code, close_msg)

def on_open(ws):
    global ws_global
    ws_global = ws
    print("### WebSocket opened ###")
    ws.send(stomp_connect(id))
    time.sleep(0.2)
    ws.send(stomp_subscribe("/user/queue/reply", sid=f"reply-{id}"))

def start_websocket():
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

# ---------------- Main ---------------- #
if __name__ == '__main__':
    threading.Thread(target=input_loop, daemon=True).start()
    connect_to_mysql()
    start_websocket()
