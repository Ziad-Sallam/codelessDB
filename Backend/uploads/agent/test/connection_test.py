## connection_test.py

import asyncio
import websockets
import json
import threading

connected_ws = None

async def handle(ws):
    global connected_ws
    connected_ws = ws

    print("Client connected!")
    # await ws.send(json.dumps({"message": "Welcome to the test server!"}))

    try:
        while True:
            msg = await ws.recv()
            print("Client says:", msg)

    except websockets.ConnectionClosed:
        print("Client disconnected.")
    finally:
        connected_ws = None


def input_sender_loop(loop):
    global connected_ws
    asyncio.set_event_loop(loop)

    while True:
        text = input()
        if connected_ws is not None:
            asyncio.run_coroutine_threadsafe(
                connected_ws.send(text),
                loop
            )
        else:
            print("No client connected.")


async def main():
    loop = asyncio.get_running_loop()
    threading.Thread(target=input_sender_loop, args=(loop,), daemon=True).start()

    server = await websockets.serve(handle, "0.0.0.0", 8765)
    print("Server running on ws://localhost:8765")
    await server.wait_closed()

asyncio.run(main())
