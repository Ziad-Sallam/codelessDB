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
def generate_key():
    key = Fernet.generate_key()
    with open(KEY_FILE, "wb") as f:
        f.write(key)
    return key

def load_key():
    if os.path.exists(KEY_FILE):
        return open(KEY_FILE, "rb").read()
    else:
        return generate_key()

fernet = Fernet(load_key())

def encrypt_password(password: str) -> str:
    return fernet.encrypt(password.encode()).decode()

def decrypt_password(token: str) -> str:
    return fernet.decrypt(token.encode()).decode()

# ---------------- Config file ----------------
def load_or_create_config():
    if os.path.exists(CONFIG_FILE):
        with open(CONFIG_FILE, "r") as f:
            config = json.load(f)
            # decrypt password
            config["password"] = decrypt_password(config["password"])
            return config
    else:
        config = {}
        config["host"] = "localhost"  if len(argv) > 3 else input("MySQL host (e.g., localhost): ")
        config["port"] = argv[4]  if len(argv) > 4 else int(input("MySQL port (e.g., 3306): "))
        config["user"] = argv[5]  if len(argv) > 5 else input("MySQL user: ")
        password = argv[6]  if len(argv) > 6 else input("MySQL password: ")
        config["password"] = encrypt_password(password)  # store encrypted
        config["database"] = argv[7]  if len(argv) > 7 else input("MySQL database name: ")
        
        

        with open(CONFIG_FILE, "w") as f:
            json.dump(config, f, indent=4)

        # decrypt before returning
        config["password"] = password
        return config

# ---------------- Globals ----------------
config = load_or_create_config()

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

# ---------------- WebSocket ----------------
def on_message(ws, message):
    global cursor, connection
    print("SERVER:", message)
    ensure_mysql_connection()
    try:
        cursor.execute(message) # type: ignore
        if cursor.with_rows: # type: ignore
            rows = cursor.fetchall() # type: ignore
            for row in rows:
                print(row)
        else:
            connection.commit() # type: ignore
    except (OperationalError, InterfaceError) as e:
        print("MySQL lost connection. Reconnecting...", e)
        connect_to_mysql()
        try:
            cursor.execute(message) # type: ignore
            if cursor.with_rows: # type: ignore
                rows = cursor.fetchall() # type: ignore
                for row in rows:
                    print(row)
            else:
                connection.commit() # type: ignore
        except Exception as e2:
            print("Failed to execute SQL after reconnect:", e2)
    except Exception as e:
        print("SQL Error:", e)

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
    ws.send(json.dumps({"command": "start"}))

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

# ---------------- Main ----------------
if __name__ == '__main__':
    threading.Thread(target=input_loop, daemon=True).start()
    connect_to_mysql()
    start_websocket()
