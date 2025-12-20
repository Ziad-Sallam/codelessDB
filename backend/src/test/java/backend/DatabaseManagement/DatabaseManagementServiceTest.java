package backend.DatabaseManagement;


import backend.databaseManagement.CreateDatabaseDTO;
import backend.databaseManagement.DatabaseManagementService;
import backend.databaseManagement.InitiateDatabaseDTO;
import backend.databaseManagement.UserDatabaseRepository;
import backend.entities.*;
import backend.user.UserRepository;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseManagementServiceTest {

    private UserDatabaseRepository userDatabaseRepository;
    private UserRepository userRepository;
    private DatabaseManagementService service;

    @BeforeEach
    void setUp() {
        userDatabaseRepository = mock(UserDatabaseRepository.class);
        userRepository = mock(UserRepository.class);
        service = new DatabaseManagementService(userDatabaseRepository, userRepository);
    }

    @Test
    void testCreateDatabase_success() throws BadRequestException {
        CreateDatabaseDTO dto = new CreateDatabaseDTO();
        dto.setDatabaseName("TestDB");
        dto.setDatabasePassword("password123");
        dto.setDdl("CREATE TABLE test(id INT)");

        User owner = new User();
        owner.setId(1);
        when(userRepository.findById(1)).thenReturn(owner);

        doAnswer(invocation -> {
            UserDatabase db = invocation.getArgument(0);
            db.setId(100);
            return null;
        }).when(userDatabaseRepository).save(any(UserDatabase.class));

        int generatedId = service.createDatabase(dto, 1);
        assertEquals(100, generatedId);
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

        User owner = new User();
        owner.setId(1);
        when(userRepository.findById(1)).thenReturn(owner);

        doThrow(new RuntimeException("DB save failed")).when(userDatabaseRepository).save(any(UserDatabase.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createDatabase(dto, 1);
        });
        assertEquals("DB save failed", exception.getMessage());
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
}