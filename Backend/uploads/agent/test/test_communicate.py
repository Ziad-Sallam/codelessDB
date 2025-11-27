import unittest
import communicate
from cryptography.fernet import Fernet
from communicate import connect_to_mysql, encrypt_password, decrypt_password, execute_sql, mysql_value_to_json, ensure_mysql_connection, parse_stomp_message
from unittest.mock import MagicMock, Mock, patch
import datetime
from mysql.connector.errors import OperationalError
import decimal
import uuid
import base64
import time
import json
import pytest
import communicate

@pytest.mark.parametrize("password", [
    "password123",
    "MyS3cretP@ssw0rd",
    "123456",
    "こんにちは",  
    ""  
])
def test_encrypt_decrypt(password):
    """Test encryption and decryption with various passwords."""
    if communicate.fernet is None:
        
        test_key = Fernet.generate_key()
        with open("test_key.key", "wb") as f:
            f.write(test_key)
        communicate.init_fernet("test_key.key")
    
    encrypted = communicate.encrypt_password(password)
    decrypted = communicate.decrypt_password(encrypted)
    assert decrypted == password

# Test execute_sql function
def test_execute_sql_select_query():
    """Test SELECT query execution with results."""
    mock_cursor = Mock()
    mock_cursor.execute = Mock()
    mock_cursor.description = [
        ('id',), ('name',), ('created_at',)
    ]
    mock_cursor.fetchall = Mock(return_value=[
        (1, 'Alice', datetime.datetime(2024, 1, 1, 12, 0, 0)),
        (2, 'Bob', datetime.datetime(2024, 1, 2, 13, 0, 0))
    ])
    
    result = execute_sql(mock_cursor, "SELECT * FROM users")
    
    assert result["success"] == True
    assert result["type"] == "SELECT"
    assert result["columns"] == ["id", "name", "created_at"]
    assert result["rowCount"] == 2
    assert len(result["rows"]) == 2
    assert result["rows"][0][0] == "1"
    assert result["rows"][0][1] == "Alice"
    mock_cursor.execute.assert_called_once_with("SELECT * FROM users", [])


def test_execute_sql_select_empty_result():
    """Test SELECT query with no results."""
    mock_cursor = Mock()
    mock_cursor.execute = Mock()
    mock_cursor.description = [('id',), ('name',)]
    mock_cursor.fetchall = Mock(return_value=[])
    
    result = execute_sql(mock_cursor, "SELECT * FROM users WHERE id = 999")
    
    assert result["success"] == True
    assert result["type"] == "SELECT"
    assert result["rowCount"] == 0
    assert result["rows"] == []


def test_execute_sql_insert_query():
    """Test INSERT query execution."""
    mock_cursor = Mock()
    mock_cursor.execute = Mock()
    mock_cursor.description = None
    mock_cursor.rowcount = 1
    
    result = execute_sql(
        mock_cursor, 
        "INSERT INTO users (name) VALUES (%s)", 
        ["Charlie"]
    )
    
    assert result["success"] == True
    assert result["type"] == "NON-SELECT"
    assert result["rowCount"] == 1
    assert result["message"] == "Query executed successfully."
    mock_cursor.execute.assert_called_once_with(
        "INSERT INTO users (name) VALUES (%s)", 
        ["Charlie"]
    )


def test_execute_sql_update_query():
    """Test UPDATE query execution."""
    mock_cursor = Mock()
    mock_cursor.execute = Mock()
    mock_cursor.description = None
    mock_cursor.rowcount = 3
    
    result = execute_sql(
        mock_cursor,
        "UPDATE users SET active = 1 WHERE created_at < '2024-01-01'"
    )
    
    assert result["success"] == True
    assert result["type"] == "NON-SELECT"
    assert result["rowCount"] == 3


def test_execute_sql_delete_query():
    """Test DELETE query execution."""
    mock_cursor = Mock()
    mock_cursor.execute = Mock()
    mock_cursor.description = None
    mock_cursor.rowcount = 2
    
    result = execute_sql(mock_cursor, "DELETE FROM users WHERE id = 5")
    
    assert result["success"] == True
    assert result["type"] == "NON-SELECT"
    assert result["rowCount"] == 2


def test_execute_sql_with_params():
    """Test query execution with parameters."""
    mock_cursor = Mock()
    mock_cursor.execute = Mock()
    mock_cursor.description = None
    mock_cursor.rowcount = 1
    
    params = ["test@example.com", "John"]
    result = execute_sql(
        mock_cursor,
        "UPDATE users SET email = %s WHERE name = %s",
        params
    )
    
    assert result["success"] == True
    mock_cursor.execute.assert_called_once_with(
        "UPDATE users SET email = %s WHERE name = %s",
        params
    )


def test_execute_sql_syntax_error():
    """Test handling of SQL syntax error."""
    mock_cursor = Mock()
    mock_cursor.execute = Mock(
        side_effect=Exception("You have an error in your SQL syntax")
    )
    
    result = execute_sql(mock_cursor, "SELCT * FROM users")
    
    assert result["success"] == False
    assert "error in your SQL syntax" in result["message"]


def test_execute_sql_connection_error():
    """Test handling of connection error."""
    mock_cursor = Mock()
    mock_cursor.execute = Mock(
        side_effect=OperationalError("Lost connection to MySQL server")
    )
    
    result = execute_sql(mock_cursor, "SELECT * FROM users")
    
    assert result["success"] == False
    assert "Lost connection" in result["message"]


def test_execute_sql_table_not_exists():
    """Test handling of non-existent table."""
    mock_cursor = Mock()
    mock_cursor.execute = Mock(
        side_effect=Exception("Table 'db.nonexistent' doesn't exist")
    )
    
    result = execute_sql(mock_cursor, "SELECT * FROM nonexistent")
    
    assert result["success"] == False
    assert "doesn't exist" in result["message"]


def test_execute_sql_with_null_values():
    """Test SELECT query with NULL values."""
    mock_cursor = Mock()
    mock_cursor.execute = Mock()
    mock_cursor.description = [('id',), ('name',), ('email',)]
    mock_cursor.fetchall = Mock(return_value=[
        (1, 'Alice', None),
        (2, None, 'bob@example.com')
    ])
    
    result = execute_sql(mock_cursor, "SELECT * FROM users")
    
    assert result["success"] == True
    assert result["rows"][0][2] is None  # NULL email
    assert result["rows"][1][1] is None  # NULL name


def test_execute_sql_with_special_types():
    """Test SELECT query with special MySQL types."""
    mock_cursor = Mock()
    mock_cursor.execute = Mock()
    mock_cursor.description = [
        ('date_col',), ('decimal_col',), ('uuid_col',), ('bytes_col',)
    ]
    mock_cursor.fetchall = Mock(return_value=[
        (
            datetime.date(2024, 1, 1),
            decimal.Decimal('123.45'),
            uuid.UUID('12345678-1234-5678-1234-567812345678'),
            b'binary_data'
        )
    ])
    
    result = execute_sql(mock_cursor, "SELECT * FROM special_types")
    
    assert result["success"] == True
    assert result["rows"][0][0] == "2024-01-01"
    assert result["rows"][0][1] == 123.45
    assert result["rows"][0][2] == "12345678-1234-5678-1234-567812345678"
    assert isinstance(result["rows"][0][3], str) 

def test_mysql_value_to_json_none():
    """Test conversion of None value."""
    result = mysql_value_to_json(None)
    assert result is None


def test_mysql_value_to_json_datetime():
    """Test conversion of datetime.datetime."""
    dt = datetime.datetime(2024, 1, 15, 14, 30, 45)
    result = mysql_value_to_json(dt)
    assert result == "2024-01-15 14:30:45"


def test_mysql_value_to_json_date():
    """Test conversion of datetime.date."""
    d = datetime.date(2024, 1, 15)
    result = mysql_value_to_json(d)
    assert result == "2024-01-15"


def test_mysql_value_to_json_time():
    """Test conversion of datetime.time."""
    t = datetime.time(14, 30, 45)
    result = mysql_value_to_json(t)
    assert result == "14:30:45"


def test_mysql_value_to_json_decimal():
    """Test conversion of Decimal."""
    dec = decimal.Decimal('123.456')
    result = mysql_value_to_json(dec)
    assert result == 123.456
    assert isinstance(result, float)


def test_mysql_value_to_json_uuid():
    """Test conversion of UUID."""
    u = uuid.UUID('12345678-1234-5678-1234-567812345678')
    result = mysql_value_to_json(u)
    assert result == "12345678-1234-5678-1234-567812345678"
    assert isinstance(result, str)


def test_mysql_value_to_json_bytes():
    """Test conversion of bytes."""
    b = b'hello world'
    result = mysql_value_to_json(b)
    expected = base64.b64encode(b).decode("utf-8")
    assert result == expected
    assert isinstance(result, str)


def test_mysql_value_to_json_bytearray():
    """Test conversion of bytearray."""
    ba = bytearray(b'test data')
    result = mysql_value_to_json(ba)
    expected = base64.b64encode(ba).decode("utf-8")
    assert result == expected
    assert isinstance(result, str)


def test_mysql_value_to_json_list():
    """Test conversion of list."""
    lst = [1, 2, 3, datetime.date(2024, 1, 1)]
    result = mysql_value_to_json(lst)
    assert result == ["1", "2", "3", "2024-01-01"]
    assert isinstance(result, list)


def test_mysql_value_to_json_tuple():
    """Test conversion of tuple."""
    tpl = (1, 'test', None)
    result = mysql_value_to_json(tpl)
    assert result == ["1", "test", None]
    assert isinstance(result, list)


def test_mysql_value_to_json_string():
    """Test conversion of string (should return as-is)."""
    s = "hello world"
    result = mysql_value_to_json(s)
    assert result == "hello world"


def test_mysql_value_to_json_integer():
    """Test conversion of integer."""
    i = 42
    result = mysql_value_to_json(i)
    assert result == "42"


def test_mysql_value_to_json_float():
    """Test conversion of float."""
    f = 3.14159
    result = mysql_value_to_json(f)
    assert result == "3.14159"


def test_mysql_value_to_json_bool():
    """Test conversion of boolean."""
    assert mysql_value_to_json(True) == "True"
    assert mysql_value_to_json(False) == "False"


def test_mysql_value_to_json_nested_list():
    """Test conversion of nested list."""
    nested = [1, [2, 3], datetime.date(2024, 1, 1)]
    result = mysql_value_to_json(nested)
    assert result == ["1", ["2", "3"], "2024-01-01"]


def test_mysql_value_to_json_mixed_types():
    """Test conversion of list with mixed types."""
    mixed = [
        None,
        datetime.datetime(2024, 1, 1, 12, 0, 0),
        decimal.Decimal('99.99'),
        uuid.UUID('12345678-1234-5678-1234-567812345678'),
        b'binary'
    ]
    result = mysql_value_to_json(mixed)
    assert result[0] is None
    assert result[1] == "2024-01-01 12:00:00"
    assert result[2] == 99.99
    assert result[3] == "12345678-1234-5678-1234-567812345678"
    assert isinstance(result[4], str)  # Base64 encoded


# Test ensure_mysql_connection function
from unittest.mock import Mock
import communicate

def test_ensure_mysql_connection_when_connected():
    global connection

    # Mock a connected connection
    mock_conn = Mock()
    mock_conn.is_connected = Mock(return_value=True)
    communicate.connection = mock_conn

    # Mock config and docker_client
    communicate.config = {
        "database": "test_db",
        "host": "localhost",
        "port": 3306,
        "user": "root",
        "password": "",
        "id": "test"
    }
    communicate.docker_client = Mock()
    communicate.docker_client.containers.get.return_value.status = "running"

    # Call the function
    communicate.ensure_mysql_connection()

    # Assert the connection check was called
    mock_conn.is_connected.assert_called_once()



import communicate
from unittest.mock import Mock

def test_ensure_mysql_connection_when_disconnected(monkeypatch):
    """Test ensure_mysql_connection when disconnected."""
    
    # Mock a disconnected connection
    mock_conn = Mock()
    mock_conn.is_connected = Mock(return_value=False)
    communicate.connection = mock_conn

    # Provide a dummy config and docker client
    communicate.config = {
        "database": "test_db",
        "host": "localhost",
        "port": 3306,
        "user": "root",
        "password": "",
        "id": "test"
    }
    communicate.docker_client = Mock()
    communicate.docker_client.containers.get.return_value.status = "running"

    # Mock connect_to_mysql to avoid actual DB connection
    reconnect_called = False
    def mock_connect():
        nonlocal reconnect_called
        reconnect_called = True

    monkeypatch.setattr(communicate, "connect_to_mysql", mock_connect)

    # Call the function under test
    communicate.ensure_mysql_connection()

    # Ensure reconnect was attempted
    assert reconnect_called is True



def test_ensure_mysql_connection_when_none(monkeypatch):
    # Make sure connection is None
    communicate.connection = None

    reconnect_called = False
    def mock_connect():
        nonlocal reconnect_called
        reconnect_called = True

    # Patch the function in the communicate module
    monkeypatch.setattr(communicate, "connect_to_mysql", mock_connect)

    # Call ensure_mysql_connection
    communicate.ensure_mysql_connection()

    # Check that reconnect was attempted
    assert reconnect_called is True


# ---------------- Test: container already running ----------------
def test_ensure_container_running_already_running():
    mock_container = Mock()
    mock_container.status = "running"

    communicate.docker_client = Mock()
    communicate.docker_client.containers.get.return_value = mock_container

    # Should not attempt to start, just pass
    communicate.ensure_container_running("test_container")
    communicate.docker_client.containers.get.assert_called_once_with("test_container")
    assert mock_container.start.call_count == 0

# ---------------- Test: container stopped ----------------
def test_ensure_container_running_stopped(monkeypatch):
    mock_container = Mock()
    mock_container.status = "exited"
    mock_container.start = Mock()

    communicate.docker_client = Mock()
    communicate.docker_client.containers.get.return_value = mock_container

    # Patch sleep to avoid waiting 10 seconds
    monkeypatch.setattr("time.sleep", lambda x: None)

    communicate.ensure_container_running("test_container")

    communicate.docker_client.containers.get.assert_called_once_with("test_container")
    mock_container.start.assert_called_once()

# ---------------- Test: container not found ----------------
def test_ensure_container_running_not_found(monkeypatch):
    communicate.docker_client = Mock()
    communicate.docker_client.containers.get.side_effect = Exception("Not found")

    # Patch exit to avoid stopping the test
    exit_called = {}
    def mock_exit(code):
        exit_called["called"] = True
        exit_called["code"] = code
        raise SystemExit(code)

    monkeypatch.setattr("builtins.exit", mock_exit)

    with pytest.raises(SystemExit):
        communicate.ensure_container_running("missing_container")

    assert exit_called.get("called") is True
    assert exit_called.get("code") == 1


# ---------------- Test: valid STOMP frame with JSON body ----------------
def test_parse_stomp_message_valid_json():
    frame = "MESSAGE\nheader1:value1\nheader2:value2\n\n{\"key\": \"value\"}\x00"
    result = parse_stomp_message(frame)
    assert result["command"] == "MESSAGE"
    assert result["headers"] == {"header1": "value1", "header2": "value2"}
    assert result["body"] == '{"key": "value"}'
    assert result["json"] == {"key": "value"}

# ---------------- Test: invalid JSON in body ----------------
def test_parse_stomp_message_invalid_json():
    frame = "MESSAGE\nheader1:value1\n\n{invalid_json}\x00"
    result = parse_stomp_message(frame)
    assert result["command"] == "MESSAGE"
    assert result["headers"] == {"header1": "value1"}
    assert result["body"] == "{invalid_json}"
    assert result["json"] is None

# ---------------- Test: missing body ----------------
def test_parse_stomp_message_missing_body():
    frame = "MESSAGE\nheader1:value1\n\n"
    result = parse_stomp_message(frame)
    assert result["command"] == "MESSAGE"
    assert result["headers"] == {"header1": "value1"}
    assert result["body"] == ""
    assert result["json"] is None

# ---------------- Test: invalid frame (no blank line) ----------------
def test_parse_stomp_message_invalid_frame():
    frame = "MESSAGE\nheader1:value1"
    result = parse_stomp_message(frame)
    assert result is None

# ---------------- Test: extra null byte ----------------
def test_parse_stomp_message_extra_null():
    frame = "MESSAGE\nheader1:value1\n\n{\"foo\": 123}\x00\x00\x00"
    result = parse_stomp_message(frame)
    assert result["body"] == '{"foo": 123}'
    assert result["json"] == {"foo": 123}


def test_connect_to_mysql_success():
    """Test successful MySQL connection."""
    mock_conn = Mock()
    mock_conn.is_connected.return_value = True
    mock_cursor = Mock()
    mock_conn.cursor.return_value = mock_cursor

    communicate.config = {
        "host": "localhost",
        "port": 3306,
        "user": "root",
        "password": "pass",
        "database": "test_db"
    }

    with patch("communicate.mysql.connector.connect", return_value=mock_conn) as mock_connect, \
         patch("communicate.ensure_container_running") as mock_ensure, \
         patch("communicate.time.sleep", return_value=None):
        conn, cursor = connect_to_mysql()
        # Assertions
        mock_connect.assert_called_once_with(
            host="localhost",
            port=3306,
            user="root",
            password="pass",
            database="test_db"
        )
        mock_ensure.assert_called()
        assert conn is mock_conn
        assert cursor is mock_cursor

def test_ensure_mysql_connection_already_connected():
    """Ensure function does not reconnect if already connected."""
    mock_conn = Mock()
    mock_conn.is_connected.return_value = True
    communicate.connection = mock_conn
    communicate.cursor = Mock()

    with patch("communicate.connect_to_mysql") as mock_connect:
        communicate.ensure_mysql_connection()
        mock_connect.assert_not_called()

def test_ensure_mysql_connection_when_disconnected():
    """Ensure function reconnects if connection lost."""
    mock_conn = Mock()
    mock_conn.is_connected.return_value = False
    communicate.connection = mock_conn
    communicate.cursor = Mock()

    with patch("communicate.connect_to_mysql") as mock_connect:
        communicate.ensure_mysql_connection()
        mock_connect.assert_called_once()


def mock_stomp_send(destination, body, content_type="application/json"):
    return f"SEND {destination}: {body}"

communicate.stomp_send = mock_stomp_send

def test_on_error_logs_error():
    ws = Mock()
    error_msg = "Test error"
    with patch("builtins.print") as mock_print:
        communicate.on_error(ws, error_msg)
        mock_print.assert_called_with("WebSocket error:", error_msg)

def test_on_message_success():
    ws = Mock()
    communicate.cursor = Mock()
    communicate.connection = Mock()
    communicate.connection.commit = Mock()

    # Mock message
    message_content = {"content": "SELECT 1", "correlationId": "abc123"}
    stomp_frame = f"MESSAGE\nheader1:val1\n\n{json.dumps(message_content)}\x00"

    with patch("communicate.ensure_mysql_connection") as mock_ensure, \
         patch("communicate.execute_sql", return_value={"type": "SELECT", "rows": [[1]]}) as mock_exec:

        communicate.on_message(ws, stomp_frame)

        mock_ensure.assert_called_once()

        mock_exec.assert_called_once_with(communicate.cursor, "SELECT 1")

        ws.send.assert_called()
        sent_payload = ws.send.call_args[0][0]
        assert "abc123" in sent_payload
        assert "rows" in sent_payload


def test_on_message_sql_failure_reconnect():
    ws = Mock()
    communicate.cursor = Mock()
    communicate.connection = Mock()
    communicate.connection.commit = Mock()

    message_content = {"content": "SELECT 1", "correlationId": "abc123"}
    stomp_frame = f"MESSAGE\nheader1:val1\n\n{json.dumps(message_content)}\x00"

    mock_exec = Mock(side_effect=[OperationalError("Lost connection"), {"type": "SELECT", "rows": [[1]]}])

    with patch("communicate.ensure_mysql_connection") as mock_ensure, \
         patch("communicate.execute_sql", mock_exec), \
         patch("communicate.connect_to_mysql") as mock_connect:

        communicate.on_message(ws, stomp_frame)

        # Should attempt reconnect
        mock_connect.assert_called_once()

        # WS send should be called twice: failed and retry success
        assert ws.send.call_count >= 1


def test_on_open_sends_stomp_commands():
    ws = Mock()
    communicate.config = {"id": "test123"}

    with patch("time.sleep", return_value=None):
        communicate.on_open(ws)

    # Should send CONNECT and SUBSCRIBE messages
    calls = [call[0][0] for call in ws.send.call_args_list]
    assert any("CONNECT" in c for c in calls)
    assert any("SUBSCRIBE" in c for c in calls)


# ----------------------------
# Test start_websocket
# ----------------------------
def test_start_websocket_runs_once(monkeypatch):
    """Test start_websocket loop runs one iteration."""
    ws_mock = Mock()
    
    # Mock WebSocketApp to return an object whose run_forever does nothing
    class MockWSApp:
        def __init__(self, url, on_open, on_message, on_error, on_close):
            self.url = url
            self.on_open = on_open
            self.on_message = on_message
            self.on_error = on_error
            self.on_close = on_close
        def run_forever(self, ping_interval=None, ping_timeout=None):
            return "ran"

    monkeypatch.setattr(communicate.websocket, "WebSocketApp", MockWSApp)
    monkeypatch.setattr("time.sleep", lambda x: None)  # avoid real sleep

    # Patch url to a dummy value
    communicate.url = "ws://dummy"

    # Run start_websocket with backoff loop limited
    with patch.object(communicate, "start_websocket", side_effect=communicate.start_websocket) as mock_start:
        # Run only one iteration by raising KeyboardInterrupt
        with patch("builtins.print") as mock_print:
            with pytest.raises(KeyboardInterrupt):
                # Force KeyboardInterrupt inside loop to exit after first iteration
                with patch("time.sleep", side_effect=KeyboardInterrupt):
                    communicate.start_websocket()

    # Should have attempted to connect
    assert any("Connecting to backend" in str(call) for call in mock_print.call_args_list)

# ----------------------------
# Test input_loop
# ----------------------------
def test_input_loop_sends_message(monkeypatch):
    ws_mock = Mock()
    communicate.ws_global = ws_mock

    # Patch input to provide one message then raise KeyboardInterrupt
    inputs = ["hello"]
    monkeypatch.setattr("builtins.input", lambda: inputs.pop(0) if inputs else KeyboardInterrupt())
    
    with patch("builtins.print") as mock_print:
        with pytest.raises(KeyboardInterrupt):
            communicate.input_loop()

    # Ensure WebSocket send called
    ws_mock.send.assert_called_once()
    sent_payload = json.loads(ws_mock.send.call_args[0][0])
    assert sent_payload["client_msg"] == "hello"


def test_main_initialization(monkeypatch):
    """Test main initializes all components."""

    # Mock init functions
    monkeypatch.setattr(communicate, "init_globals", lambda ws, sid: None)
    monkeypatch.setattr(communicate, "init_fernet", lambda: None)
    monkeypatch.setattr(communicate, "init_config", lambda: None)
    monkeypatch.setattr(communicate, "init_docker", lambda: None)
    monkeypatch.setattr(communicate, "connect_to_mysql", lambda: None)

    # Monkeypatch start_websocket to raise exception and stop the infinite loop
    def raise_keyboard_interrupt():
        raise KeyboardInterrupt()
    monkeypatch.setattr(communicate, "start_websocket", raise_keyboard_interrupt)

    with patch("builtins.print") as mock_print:
        with pytest.raises(KeyboardInterrupt):
            communicate.main("ws://dummy", "testid")
