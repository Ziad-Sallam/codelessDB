package backend.DatabaseManagement;


import backend.databaseManagement.CreateDatabaseDTO;
import backend.databaseManagement.CreateServerDTO;
import backend.databaseManagement.DatabaseManagementService;
import backend.databaseManagement.InitiateDatabaseDTO;
import backend.databaseManagement.ServerRepository;
import backend.databaseManagement.UserDatabaseRepository;
import backend.entities.*;
import backend.user.UserRepository;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DatabaseManagementServiceTest {

    private UserDatabaseRepository userDatabaseRepository;
    private UserRepository userRepository;
    private DatabaseManagementService service;
    private ServerRepository serverRepository;

    @BeforeEach
    void setUp() {
        userDatabaseRepository = mock(UserDatabaseRepository.class);
        userRepository = mock(UserRepository.class);
        serverRepository = mock(ServerRepository.class);
        
        service = new DatabaseManagementService(userDatabaseRepository, userRepository, serverRepository);
    }

    @Test
    void testCreateDatabase_success() throws BadRequestException {
        CreateDatabaseDTO dto = new CreateDatabaseDTO();
        dto.setDatabaseName("TestDB");
        dto.setDatabasePassword("password123");
        dto.setDdl("CREATE TABLE test(id INT)");
        dto.setServerId(1);

        Server server = new Server();
        server.setId(1);
        when(serverRepository.findById(1)).thenReturn(Optional.of(server));

        User owner = new User();
        owner.setId(1);
        owner.getServers().add(server);
        when(userRepository.findById(1)).thenReturn(owner);

        doAnswer(invocation -> {
            UserDatabase db = invocation.getArgument(0);
            db.setId(100);
            return null;
        }).when(userDatabaseRepository).save(any(UserDatabase.class));

        CreateDatabaseDTO generatedId = service.createDatabase(dto, 1);
        assertEquals(100, generatedId.getDatabaseId());
    }

    @Test
    void testCreateDatabase_nullDTO() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createDatabase(null, 1);
        });
    }

    @Test
    void testCreateDatabase_invalidOwner() {
        CreateDatabaseDTO dto = new CreateDatabaseDTO();
        dto.setDatabaseName("DB");
        dto.setDatabasePassword("pwd");
        dto.setDdl("DDL");

        when(userRepository.findById(999)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createDatabase(dto, 999);
        });
    }

    @Test
    void testCreateDatabase_repositoryThrowsException() {
        CreateDatabaseDTO dto = new CreateDatabaseDTO();

        dto.setDatabaseName("DB");
        dto.setDatabasePassword("pwd");
        dto.setDdl("DDL");
        dto.setServerId(1);

        Server server = new Server();
        server.setId(1);
        when(serverRepository.findById(1)).thenReturn(Optional.of(server));

        User owner = new User();
        owner.setId(1);
        owner.getServers().add(server);
        when(userRepository.findById(1)).thenReturn(owner);

        doThrow(new RuntimeException("DB save failed")).when(userDatabaseRepository).save(any(UserDatabase.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createDatabase(dto, 1);
        });
        assertEquals("DB save failed", exception.getMessage());
    }

    @Test
    void testCreateDatabase_nullServerId_nullServerName(){
        CreateDatabaseDTO dto = new CreateDatabaseDTO(); 
        dto.setDatabaseName("TestDB");
        dto.setDatabasePassword("password123");
        dto.setDdl("CREATE TABLE test(id INT)");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.createDatabase(dto, 0);
        });
        assertEquals("serverName is required when serverId is null", ex.getMessage());

    }

    @Test
    void testCreateDatabase_nullDatabaseName(){
        CreateDatabaseDTO dto = new CreateDatabaseDTO(); 
        
        dto.setDatabasePassword("password123");
        dto.setDdl("CREATE TABLE test(id INT)");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.createDatabase(dto, 0);
        });
        assertEquals("Missing required fields", ex.getMessage());

    }

    @Test
    void testCreateDatabase_nullDatabasePassword(){
        CreateDatabaseDTO dto = new CreateDatabaseDTO(); 
        dto.setDatabaseName("TestDB");
        dto.setDdl("CREATE TABLE test(id INT)");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.createDatabase(dto, 0);
        });
        assertEquals("Missing required fields", ex.getMessage());

    }

    @Test
    void testCreateDatabase_nullServerId_validServerName() {
        // DTO with null serverId but serverName provided
        CreateDatabaseDTO dto = new CreateDatabaseDTO();
        dto.setDatabaseName("TestDB");
        dto.setDatabasePassword("password123");
        dto.setDdl("CREATE TABLE test(id INT)");
        dto.setServerName("Server");

        // Mock owner
        User owner = new User();
        owner.setId(1);
        owner.setServers(new ArrayList<>());
        when(userRepository.findById(1)).thenReturn(owner);

        // Mock serverRepository.save to assign ID when creating server
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> {
            Server s = invocation.getArgument(0);
            s.setId(10); // Simulate DB-generated ID
            return s;
        });

        // Mock serverRepository.findById after creation
        when(serverRepository.findById(10)).thenReturn(Optional.of(new Server() {{
            setId(10);
            setName("Server");
            setOwner(owner);
        }}));

        // Mock userDatabaseRepository.save to assign DB ID
        doAnswer(invocation -> {
            UserDatabase db = invocation.getArgument(0);
            db.setId(100);
            return null;
        }).when(userDatabaseRepository).save(any(UserDatabase.class));

        // Call the service
        CreateDatabaseDTO result = service.createDatabase(dto, 1);

        // Assertions
        assertEquals(10, result.getServerId());           // Server ID set
        assertEquals(100, result.getDatabaseId());       // Database ID set
        assertEquals("Server", result.getServerName());  // Server name remains
        assertEquals("TestDB", result.getDatabaseName());

        // Verify interactions
        verify(serverRepository, times(1)).save(any(Server.class));
        verify(userDatabaseRepository, times(1)).save(any(UserDatabase.class));
        verify(userRepository, times(1)).save(owner);
    }

    @Test
    void testCreateDatabase_unauthorizedAccess_throwsException() {
        // DTO with serverId
        CreateDatabaseDTO dto = new CreateDatabaseDTO();
        dto.setDatabaseName("TestDB");
        dto.setDatabasePassword("password");
        dto.setServerId(1);

        // Owner with empty servers (no access)
        User owner = new User();
        owner.setId(1);
        owner.setServers(new ArrayList<>());
        when(userRepository.findById(1)).thenReturn(owner);

        // Server exists
        Server server = new Server();
        server.setId(1);
        when(serverRepository.findById(1)).thenReturn(Optional.of(server));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.createDatabase(dto, 1);
        });

        assertEquals("Unauthorized Access", ex.getMessage());
    }

    @Test
    void testCreateDatabase_serverNameNotFound_throwsException() {
        // DTO with null serverId and null serverName
        CreateDatabaseDTO dto = new CreateDatabaseDTO();
        dto.setDatabaseName("TestDB");
        dto.setDatabasePassword("password");

        User owner = new User();
        owner.setId(1);
        owner.setServers(new ArrayList<>());
        when(userRepository.findById(1)).thenReturn(owner);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.createDatabase(dto, 1);
        });

        assertEquals("serverName is required when serverId is null", ex.getMessage());
    }

    @Test
    void testInitiateDatabase_success() throws BadRequestException {
        UserDatabase database = new UserDatabase();
        database.setId(10);
        database.setName("InitDB");
        database.setPassword("pwd");
        database.setDdl("CREATE TABLE test2(id INT)");

        when(userDatabaseRepository.findById(10)).thenReturn(Optional.of(database));

        InitiateDatabaseDTO dto = service.initiateDatabase(10);
        assertEquals("InitDB", dto.getDatabaseName());
        assertEquals("pwd", dto.getPassword());
        assertEquals("CREATE TABLE test2(id INT)", dto.getDdl());
        assertEquals(10, dto.getContainerId());
        assertEquals("ws://localhost:8080/agent-ws", dto.getWsUrl());
        assertEquals("mysql10", dto.getContainerName());
    }

    @Test
    void testInitiateDatabase_notFound() {
        when(userDatabaseRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.initiateDatabase(99);
        });
        assertEquals("Database not found", exception.getMessage());
    }

    @Test
    void testInitiateDatabase_invalidId_throwsBadRequest() {
        int invalidId = 0; // or any negative number

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            service.initiateDatabase(invalidId);
        });

        // Optional: verify exception message
        assertEquals("Invalid database ID", exception.getMessage());
    }

    @Test
    void testCreateServer_nullDTO_throwsException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.createServer(null, 1);
        });

        assertEquals("DTO cannot be null", ex.getMessage());
    }

    @Test
    void testCreateServer_ownerNotFound_throwsException() {
        CreateServerDTO dto = new CreateServerDTO();
        dto.setServerName("MyServer");

        when(userRepository.findById(1)).thenReturn(null); // Owner missing

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.createServer(dto, 1);
        });

        assertEquals("Owner not found", ex.getMessage());
    }

    @Test
    void testCreateServer_success() {
        CreateServerDTO dto = new CreateServerDTO();
        dto.setServerName("MyServer");

        User owner = new User();
        owner.setId(1);
        owner.setServers(new ArrayList<>());

        when(userRepository.findById(1)).thenReturn(owner);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> {
            Server s = invocation.getArgument(0);
            s.setId(100); // Simulate DB generated ID
            return s;
        });

        CreateServerDTO result = service.createServer(dto, 1);

        assertEquals(100, result.getServerId());
        assertEquals(1, owner.getServers().size()); // Owner’s servers updated
        assertEquals("MyServer", owner.getServers().get(0).getName());

        // Verify save calls
        verify(serverRepository, times(1)).save(any(Server.class));
        verify(userRepository, times(1)).save(owner);
    }

    @Test
    void testCreateServer_nullServerName_throwsException() {
        CreateServerDTO dto = new CreateServerDTO();
        dto.setServerName(null); // explicitly null

        User owner = new User();
        owner.setId(1);
        when(userRepository.findById(1)).thenReturn(owner);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            service.createServer(dto, 1);
        });

        assertEquals("Server name Not Found !", ex.getMessage());
    }


}