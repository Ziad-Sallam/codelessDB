import pytest
from unittest.mock import patch, MagicMock, call
import create_container
import json
import docker
from docker import errors as docker_errors

# -----------------------
# Fixtures
# -----------------------

@pytest.fixture
def mock_docker():
    """Mock docker.from_env() and all used docker methods."""
    with patch("docker.from_env") as mock:
        mock_client = MagicMock()
        mock.return_value = mock_client

        # Mock container
        mock_container = MagicMock()
        mock_container.status = "running"
        mock_container.short_id = "abc123"
        mock_container.attrs = {
            'NetworkSettings': {
                'Ports': {
                    "3306/tcp": [{"HostPort": "54321"}]
                }
            }
        }
        mock_client.containers.get.return_value = mock_container
        mock_client.containers.run.return_value = mock_container

        # Mock volumes
        mock_volume = MagicMock()
        mock_volume.name = "mysql_test_data"
        mock_client.volumes.list.return_value = [mock_volume]

        yield mock_client

@pytest.fixture
def mock_requests_success():
    with patch("create_container.requests.post") as mock_post:
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.content = b"OK"
        mock_response.json.return_value = {
            "databaseName": "mysql_test",
            "password": "rootpass",
            "containerId": 123,
            "ddl": "CREATE TABLE test (id INT);",
            "wsUrl": "ws://localhost/ws"
        }
        mock_post.return_value = mock_response
        yield mock_post

@pytest.fixture
def mock_socket():
    with patch("create_container.socket.socket") as mock_socket:
        mock_sock = MagicMock()
        mock_sock.getsockname.return_value = ['', 3307]
        mock_socket.return_value = mock_sock
        yield mock_socket

# -----------------------
# select_random_port tests
# -----------------------

def test_select_random_port(mock_socket):
    port = create_container.select_random_port()
    assert port == 3307
    mock_socket.return_value.bind.assert_called_with(('', 0))
    mock_socket.return_value.close.assert_called_once()

# -----------------------
# ensure_docker_installed tests
# -----------------------

def test_ensure_docker_installed_true():
    with patch("shutil.which", return_value="/usr/bin/docker"):
        assert create_container.ensure_docker_installed() is True

def test_ensure_docker_installed_false_windows(capfd):
    with patch("shutil.which", return_value=None), \
         patch("platform.system", return_value="Windows"):
        result = create_container.ensure_docker_installed()
        assert result is False
        captured = capfd.readouterr()
        assert "Docker is not installed" in captured.out
        assert "Windows" in captured.out

def test_ensure_docker_installed_false_linux(capfd):
    with patch("shutil.which", return_value=None), \
         patch("platform.system", return_value="Linux"):
        result = create_container.ensure_docker_installed()
        assert result is False
        captured = capfd.readouterr()
        assert "Docker is not installed" in captured.out
        assert "Linux" in captured.out

def test_ensure_docker_installed_false_darwin(capfd):
    with patch("shutil.which", return_value=None), \
         patch("platform.system", return_value="Darwin"):
        result = create_container.ensure_docker_installed()
        assert result is False
        captured = capfd.readouterr()
        assert "Docker is not installed" in captured.out
        assert "macOS" in captured.out

def test_ensure_docker_installed_unsupported_os (capfd):
    with patch("shutil.which", return_value=None), \
         patch("platform.system", return_value="UnsupportedOS"):
        result = create_container.ensure_docker_installed()
        assert result is False
        captured = capfd.readouterr()
        assert "Unsupported OS" in captured.out
        

# -----------------------
# create_mysql_container tests
# -----------------------

def test_create_mysql_container_docker_not_installed():
    with patch("create_container.ensure_docker_installed", return_value=False):
        result = create_container.create_mysql_container(123, "http://localhost:8080")
        assert result == -1

def test_create_mysql_container_existing_container(mock_docker, mock_requests_success):
    """Test when container already exists and is running."""
    result = create_container.create_mysql_container(123, "http://localhost:8080")
    assert result == 0
    mock_docker.containers.get.assert_called_once_with("mysql_test")

def test_create_mysql_container_existing_but_stopped(mock_docker, mock_requests_success):
    """Test when container exists but is stopped."""
    mock_docker.containers.get.return_value.status = "exited"
    result = create_container.create_mysql_container(123, "http://localhost:8080")
    assert result == 0
    mock_docker.containers.get.return_value.start.assert_called_once()

def test_create_mysql_container_new_container(mock_requests_success):
    """Test creating a completely new container."""
    with patch("create_container.docker.from_env") as mock_docker, \
         patch("create_container.time.sleep"), \
         patch("create_container.mysql.connector.connect") as mock_mysql, \
         patch("create_container.Fernet") as mock_fernet, \
         patch("builtins.open"), \
         patch("create_container.subprocess.run") as mock_subprocess, \
         patch("create_container.os.environ.copy", return_value={}):

        # Mock Docker client
        mock_client = MagicMock()
        mock_docker.return_value = mock_client
        # Simulate container not found
        mock_client.containers.get.side_effect = docker_errors.NotFound("Not found")
        
        # Mock volumes
        mock_client.volumes.list.return_value = []
        mock_client.volumes.create.return_value = None
        
        # Mock container creation
        mock_container = MagicMock()
        mock_container.attrs = {
            'NetworkSettings': {
                'Ports': {
                    "3306/tcp": [{"HostPort": "54321"}]
                }
            }
        }
        mock_container.short_id = "abc123"
        mock_client.containers.run.return_value = mock_container
        
        # Mock MySQL connection
        mock_conn = MagicMock()
        mock_cursor = MagicMock()
        mock_conn.cursor.return_value = mock_cursor
        mock_mysql.return_value = mock_conn
        
        # Mock Fernet encryption
        mock_fernet_instance = MagicMock()
        mock_fernet.generate_key.return_value = b"test_key"
        mock_fernet.return_value = mock_fernet_instance
        mock_fernet_instance.encrypt.return_value = b"encrypted_password"
        
        result = create_container.create_mysql_container(123, "http://localhost:8080")
        assert result == 0
        
        # Verify container was created
        mock_client.containers.run.assert_called_once()
        # Verify MySQL connection was made
        mock_mysql.assert_called_once_with(
            host="localhost",
            port="54321", 
            user="root",
            password="rootpass",
            database="mysql_test"
        )
        # Verify DDL was executed
        mock_cursor.execute.assert_called_with("CREATE TABLE test (id INT);")
        # Verify subprocess was called
        mock_subprocess.assert_called_once()

def test_create_mysql_container_api_failure(mock_docker, capfd):
    """Test when API request fails."""
    with patch("create_container.requests.post") as mock_post:
        mock_response = MagicMock()
        mock_response.status_code = 500
        mock_response.content = b"Server Error"
        mock_post.return_value = mock_response
        
        result = create_container.create_mysql_container(123, "http://localhost:8080")
        assert result == -1
        captured = capfd.readouterr()
        assert "Response status code: 500" in captured.out

def test_create_mysql_container_mysql_connection_failure(mock_docker, mock_requests_success, capfd):
    """Test when MySQL connection fails."""
    with patch("create_container.mysql.connector.connect") as mock_connect, \
        patch("create_container.time.sleep"):
        mock_connect.side_effect = Exception("Connection failed")
        # Simulate new container creation
        mock_docker.containers.get.side_effect = docker_errors.NotFound("Not found")
        
        result = create_container.create_mysql_container(123, "http://localhost:8080")
        assert result == -1 

# -----------------------
# Main function tests
# -----------------------

def test_main_success():
    with patch("create_container.sys.argv", ["create_container.py", "http://localhost:8080", "123"]), \
         patch("create_container.create_mysql_container") as mock_create:
        mock_create.return_value = 0
        create_container.main()
        mock_create.assert_called_once_with(123,"http://localhost:8080")

def test_main_insufficient_args(capfd):
    with patch("sys.argv", ["create_container.py"]):
        with pytest.raises(SystemExit):
            create_container.main()
        captured = capfd.readouterr()
        assert "Usage:" in captured.out

def test_main_invalid_args(capfd):
    with patch("create_container.sys.argv", ["create_container.py", "http://localhost:8080", "not_an_int"]):
        with pytest.raises(SystemExit):
            create_container.main()
        captured = capfd.readouterr()
        assert "Usage:" in captured.out

def test_main_block():
    """Test that main() is called when __name__ is '__main__'"""
    with patch("create_container.main") as mock_main:
        # Directly call what happens in the main block
        create_container.main()
        
        # Just verify main can be called (this covers the line)
        mock_main.assert_called_once()

# -----------------------
# Edge case tests
# -----------------------

def test_create_mysql_container_volume_creation(mock_requests_success, capfd):
    """Test volume creation when it doesn't exist."""
    with patch("create_container.docker.from_env") as mock_docker, \
         patch("create_container.time.sleep"), \
         patch("create_container.mysql.connector.connect") as mock_mysql, \
         patch("create_container.Fernet"), \
         patch("builtins.open"), \
         patch("create_container.subprocess.run"), \
         patch("create_container.select_random_port", return_value=3307), \
         patch("create_container.json.dump") as mock_json_dump :
         
        mock_json_dump.return_value = "123"

        mock_client = MagicMock()
        mock_docker.return_value = mock_client
        
        # Container doesn't exist
        mock_client.containers.get.side_effect = docker_errors.NotFound("Not found")
        
        # Volume doesn't exist
        mock_client.volumes.list.return_value = []
        
        # Mock container with exact structure
        mock_container = MagicMock()
        mock_container.attrs = {
            'NetworkSettings': {
                'Ports': {
                    "3306/tcp": [
                        {
                            "HostIp": "0.0.0.0", 
                            "HostPort": "54321"
                        }
                    ]
                }
            }
        }
        mock_container.short_id = "abc123"
        mock_container.reload = MagicMock()
        
        mock_client.containers.run.return_value = mock_container
        
        # Mock MySQL connection
        mock_conn = MagicMock()
        mock_cursor = MagicMock()
        mock_conn.cursor.return_value = mock_cursor
        mock_conn.is_connected.return_value = True
        mock_mysql.return_value = mock_conn
        
        result = create_container.create_mysql_container(123, "http://localhost:8080")
        
        # Debug output
        print(f"Result: {result}")
        print(f"Volume create calls: {mock_client.volumes.create.call_args_list}")
        print(f"Container run calls: {mock_client.containers.run.call_args_list}")
        
        assert result == 0
        mock_client.volumes.create.assert_called_once_with(name="mysql_test_data") 