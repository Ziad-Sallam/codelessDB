# MySQL Docker Client with WebSocket Backend

This project provides a Python-based system to automatically create MySQL Docker containers, manage connections, and interact with them via a WebSocket client. It includes:

* Automatic MySQL container creation (`create_container.py`)
* WebSocket client for real-time communication (`communicate.py`)
* Test WebSocket server for development (`connection_test.py`)

---

## Table of Contents

* [Features](#features)
* [Requirements](#requirements)
* [Setup](#setup)
* [Usage](#usage)
* [Environment Variables](#environment-variables)
* [File Structure](#file-structure)
* [Notes](#notes)

---

## Features

* Automatically creates MySQL containers with unique names and volumes
* Handles MySQL connection retries and reconnections
* Securely stores credentials using encrypted configuration files
* Real-time communication via WebSocket
* Supports multiple container instances and dynamic host/port retrieval

---

## Requirements

* Python 3.11
* Docker installed and running
* Required Python packages (install via `requirements.txt`):

```bash
pip install docker mysql-connector-python websocket-client cryptography requests websockets
```

* (Optional) Virtual environment is recommended:

```bash
python -m venv venv
source venv/bin/activate  # Linux/Mac
venv\Scripts\activate     # Windows
```

---

## Setup

1. Start your backend WebSocket server or test server (`connection_test.py`) on port 8000 or modify the URL in `create_container.py`.

```bash
python connection_test.py
```

2. Ensure Docker is installed and running. On Windows/macOS, Docker Desktop is recommended.

---

## Usage

### 1. Create MySQL container

```bash
python create_container.py <container_id>
```

* `<container_id>`: Unique integer identifier for your container
* This script will:

  * Request backend for container information
  * Create Docker volume and container if not existing
  * Start the container if it is stopped
  * Launch the WebSocket client (`communicate.py`) automatically

### 2. Communicate with container (handled automatically)

* `communicate.py` connects to the WebSocket backend and interacts with MySQL using credentials from the container.
* You can also run it manually if needed:

```bash
python communicate.py <websocket_url> <unique_id> <host> <port> <user> <password> <database> <container_name>
```

---

### 3. Test WebSocket Server

1. Use `connection_test.py` for development and testing:

```bash
python connection_test.py
```

  * Connects on `ws://localhost:8765`
  * Allows sending messages to connected clients
2. Use `main.py` to run the sample users
```bash
python main.py
```


---

## Environment Variables

The scripts automatically handle configuration files and encryption for credentials:

* Each client instance generates:

  * `client_key_<ID>.key` → Encryption key
  * `client_config_<ID>.json` → Encrypted credentials

* Environment variables for subprocess communication:

  * `WS_URL` → WebSocket URL

---

## File Structure

```
.
.
├── create_container.py           # Main script to create MySQL container and run client
├── communicate.py                # WebSocket client that connects to container MySQL
├── client_config_<ID>.json       # Auto-generated config files per client instance
├── client_key_<ID>.key           # Auto-generated encryption key per client
└── test/                         # Test and development scripts
    ├── connection_test.py        # Test WebSocket server for development
    ├── main.py                   # Additional test script / entry point for testing
    └── sample_data.py            # Sample data for tests

```

---

## Notes

* The main README is the entry point; submodules generate their own configuration files per container instance.
* Docker host and port are dynamically retrieved; if `HostIp` is `0.0.0.0`, it resolves to `localhost`.
* MySQL connection includes automatic reconnection logic in case the container stops or loses connection.
* WebSocket reconnection uses exponential backoff to avoid flooding the backend.
