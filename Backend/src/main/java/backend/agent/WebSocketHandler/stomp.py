import websocket
import threading
import json
import time

# Callback when a message is received from the server
def on_message(ws, message):
    print("\nReceived:", message)

def on_error(ws, error):
    print("Error:", error)

def on_close(ws, close_status_code, close_msg):
    print("Connection closed:", close_status_code, close_msg)

def on_open(ws):
    # Connect to STOMP
    ws.send("CONNECT\naccept-version:1.2\nhost:localhost\n\n\x00")
    time.sleep(0.1)

    # Subscribe to /topic/messages
    ws.send("SUBSCRIBE\ndestination:/topic/messages\nid:sub-0\n\n\x00")
    time.sleep(0.1)

    # Start a separate thread to handle user input
    def read_input():
        while True:
            try:
                msg_content = input("Enter message (or 'exit' to quit): ")
                if msg_content.lower() == "exit":
                    ws.close()
                    break
                message = {"sender": "PythonClient", "content": msg_content}
                ws.send(
                    "SEND\ndestination:/app/test\ncontent-type:application/json\n\n"
                    + json.dumps(message)
                    + "\x00"
                )
            except EOFError:
                ws.close()
                break

    threading.Thread(target=read_input, daemon=True).start()

# Create WebSocketApp
ws_app = websocket.WebSocketApp(
    "ws://localhost:8080/agent-ws",
    on_message=on_message,
    on_error=on_error,
    on_close=on_close
)

ws_app.on_open = on_open
ws_app.run_forever()
