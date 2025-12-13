package backend.DatabaseManagement;


import backend.agent.WebSocketHandler.OnlineUserTracker;
import backend.databaseManagement.CreateDatabaseDTO;
import backend.databaseManagement.CreateServerDTO;
import backend.databaseManagement.DatabaseManagementService;
import backend.databaseManagement.InitiateDatabaseDTO;
import backend.databaseManagement.Database;
import backend.databaseManagement.SendDatabasesDTO;
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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DatabaseManagementServiceTest {

    private UserDatabaseRepository userDatabaseRepository;
    private UserRepository userRepository;
    private DatabaseManagementService service;
    private ServerRepository serverRepository;
    private OnlineUserTracker tracker;

    @BeforeEach
    void setUp() {
        userDatabaseRepository = mock(UserDatabaseRepository.class);
        userRepository = mock(UserRepository.class);
        serverRepository = mock(ServerRepository.class);
        tracker = mock(OnlineUserTracker.class);
        
        service = new DatabaseManagementService(userDatabaseRepository, userRepository, serverRepository, tracker);
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

        when(userRepository.findById(1)).thenReturn(owner);
        when(serverRepository.save(any(Server.class))).thenAnswer(invocation -> {
            Server s = invocation.getArgument(0);
            s.setId(100); // Simulate DB generated ID
            return s;
        });

        CreateServerDTO result = service.createServer(dto, 1);

        assertEquals(100, result.getServerId());
        assertEquals(1, owner.getServers().size()); // Owner’s servers updated
        assertTrue(
            owner.getServers()
                .stream()
                .anyMatch(s -> "MyServer".equals(s.getName()))
        );


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

        assertEquals("Server name not found!", ex.getMessage());
    }


    @Test
    void testGetUserServers_success() {
        User user = new User();
        user.setId(1);

        Server s1 = new Server();
        s1.setId(10);
        s1.setName("ServerOne");

        Server s2 = new Server();
        s2.setId(20);
        s2.setName("ServerTwo");

        user.getServers().add(s1);
        user.getServers().add(s2);

        when(userRepository.findById(1)).thenReturn(user);

        List<CreateServerDTO> result = service.getUserServers(1);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue( result.stream().anyMatch(s -> 
            s.getServerName()=="ServerOne" && 
            s.getServerId()==10)
        );

        assertTrue(result.stream().anyMatch(s -> 
            s.getServerName()=="ServerTwo" && 
            s.getServerId()==20)
        );
    }

    @Test
    void testGetUserServers_noServers_returnsEmptyList() {
        User user = new User();
        user.setId(1);

        when(userRepository.findById(1)).thenReturn(user);

        List<CreateServerDTO> result = service.getUserServers(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserDatabases_validUser_returnsDatabases() {
        // Arrange
        int userId = 1;

        User user = new User();

        Server server = new Server();
        server.setName("Server-1");

        UserDatabase db1 = new UserDatabase();
        db1.setId(10);
        db1.setName("DB1");
        db1.setServer(server);

        UserDatabase db2 = new UserDatabase();
        db2.setId(20);
        db2.setName("DB2");
        db2.setServer(server);

        Set<UserDatabase> databases = Set.of(db1, db2);
        user.setAccessibleDatabases(databases);

        when(userRepository.findById(userId)).thenReturn(user);

        // Act
        SendDatabasesDTO result = service.getUserDatabases(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getDatabases().size());

        assertTrue(
            result.getDatabases().stream().anyMatch(db ->
                db.getDatabaseId() == 10 &&
                "DB1".equals(db.getDatabaseName()) &&
                "Server-1".equals(db.getServerName())
            )
        );

        assertTrue(
            result.getDatabases().stream().anyMatch(db ->
                db.getDatabaseId() == 20 &&
                "DB2".equals(db.getDatabaseName()) &&
                "Server-1".equals(db.getServerName())
            )
        );

        verify(userRepository).findById(userId);

    }    


    @Test
    void createDatabase_databaseNameAlreadyExists_throwsException() {
        // Arrange
        int ownerId = 1;

        CreateDatabaseDTO dto = new CreateDatabaseDTO();
        dto.setDatabaseName("test_db");
        dto.setDatabasePassword("1234");
        dto.setServerId(10);

        // User
        User owner = new User();

        // Server
        Server server = new Server();
        server.setId(10);

        // Existing database with same name & server
        UserDatabase existingDb = new UserDatabase();
        existingDb.setName("test_db");
        existingDb.setServer(server);

        owner.setServers(Set.of(server));
        owner.setAccessibleDatabases(Set.of(existingDb));

        when(userRepository.findById(ownerId)).thenReturn(owner);
        when(serverRepository.findById(10)).thenReturn(Optional.of(server));

        // Act + Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.createDatabase(dto, ownerId)
        );

        assertEquals(
                "Database name already exists on this server for this user",
                ex.getMessage()
        );

        // Verify no save happened
        verify(userDatabaseRepository, never()).save(any());
    }

}