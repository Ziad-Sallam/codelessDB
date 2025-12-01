import websocket
import threading
import json
import time

# -----------------------------
# STOMP Frame Builders
# -----------------------------

def stomp_connect(username):
    return (
        "CONNECT\n"
        "accept-version:1.2\n"
        "host:localhost\n"
        f"Authorization:{username}\n"
        "\n\x00"
    )

def stomp_subscribe(destination, sid="sub-0"):
    return (
        f"SUBSCRIBE\n"
        f"destination:{destination}\n"
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

# -----------------------------
# WebSocket Callbacks
# -----------------------------

def on_message(ws, message):
    print("\n📩 Received:", message)

def on_error(ws, error):
    print("❌ Error:", error)

def on_close(ws, close_status_code, close_msg):
    print("🔌 Connection closed:", close_status_code, close_msg)

def on_open(ws):
    print("🔗 Connected, sending STOMP CONNECT")

    # Send CONNECT with user identity
    ws.send(stomp_connect(user))

    time.sleep(0.1)

    # Subscribe to topic
    ws.send(stomp_subscribe("/user/queue/reply"))
    print("📡 Subscribed to personal queue")

    # Input thread
    def input_thread():
        while True:
            msg = input("Enter message (or 'exit'): ")
            if msg.lower() == "exit":
                ws.close()
                break

            payload = json.dumps({"sender": user, "content": msg})
            ws.send(stomp_send("/app/test", payload))
            print("📤 Sent:", msg)

    threading.Thread(target=input_thread, daemon=True).start()

# -----------------------------
# Start WebSocket Client
# -----------------------------
global user
user = str(input("Enter your username: "))
ws_app = websocket.WebSocketApp(
    "ws://localhost:8080/agent-ws",
    on_open=on_open,
    on_message=on_message,
    on_error=on_error,
    on_close=on_close
)

print("🚀 Connecting to WebSocket...")
ws_app.run_forever()
