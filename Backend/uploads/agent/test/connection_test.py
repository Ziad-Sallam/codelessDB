import asyncio
import websockets
import json
import threading

connected_ws = None

async def handle(ws):
    global connected_ws
    connected_ws = ws
    print("Client connected!")

    try:
        while True:
            msg = await ws.recv()
            print("Client says:", msg)
            # Optionally echo
            # await ws.send(f"Server received: {msg}")
    except websockets.ConnectionClosed:
        print("Client disconnected.")
    finally:
        connected_ws = None


def input_sender_loop(loop):
    global connected_ws
    while True:
        text = input()
        if connected_ws:
            try:
                asyncio.run_coroutine_threadsafe(
                    connected_ws.send(text),
                    loop
                )
            except Exception as e:
                print("Failed to send message:", e)
        else:
            print("No client connected.")


async def main():
    loop = asyncio.get_running_loop()
    threading.Thread(target=input_sender_loop, args=(loop,), daemon=True).start()

    server = await websockets.serve(handle, "0.0.0.0", 8765)
    print("Server running on ws://localhost:8765")
    await server.wait_closed()


asyncio.run(main())
