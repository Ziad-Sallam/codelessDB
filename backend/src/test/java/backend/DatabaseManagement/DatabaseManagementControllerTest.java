package backend.DatabaseManagement;

import java.util.Arrays;
import java.util.List;

import org.apache.coyote.BadRequestException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import backend.databaseManagement.DatabaseManagementController;
import backend.databaseManagement.DatabaseManagementService;
import backend.databaseManagement.dto.CreateDatabaseDTO;
import backend.databaseManagement.dto.CreateServerDTO;
import backend.databaseManagement.dto.InitiateDatabaseDTO;
import backend.databaseManagement.dto.SendDatabasesDTO;
import backend.security.AuthUser;

class DatabaseManagementControllerTest {

    @InjectMocks
    private DatabaseManagementController controller;

    @Mock
    private DatabaseManagementService databaseManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /*
     * --------------------------------------------------------
     * createDatabase
     * --------------------------------------------------------
     */
@Test
void createDatabase_success_returnsCreated() throws Exception {
    // Arrange
    CreateDatabaseDTO dto = new CreateDatabaseDTO();
    dto.setDatabaseName("TestDB");
    dto.setDatabasePassword("pwd");

    AuthUser authUser = mock(AuthUser.class);
    when(authUser.userId()).thenReturn(1);

    CreateDatabaseDTO returnedDTO = new CreateDatabaseDTO();
    returnedDTO.setDatabaseId(100);

    when(databaseManagementService.createDatabase(dto, 1)).thenReturn(returnedDTO);

    // Act
    ResponseEntity<CreateDatabaseDTO> response = controller.createDatabase(dto, authUser);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode()); // check for 201
    assertEquals(returnedDTO.getDatabaseId(), response.getBody().getDatabaseId()); // check DTO contents
}


@Test
void createDatabase_serviceThrowsException_throwsRuntimeException() {
    // Arrange
    CreateDatabaseDTO dto = new CreateDatabaseDTO();
    AuthUser authUser = mock(AuthUser.class);
    when(authUser.userId()).thenReturn(1);

    when(databaseManagementService.createDatabase(dto, 1))
            .thenThrow(new RuntimeException("DB error"));

    // Act & Assert
    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> controller.createDatabase(dto, authUser));

    assertEquals("DB error", ex.getMessage());
}


@Test
void createServer_serviceThrowsException_throwsRuntimeException() {
    // Arrange
    CreateServerDTO dto = new CreateServerDTO();
    AuthUser authUser = mock(AuthUser.class);
    when(authUser.userId()).thenReturn(1);

    when(databaseManagementService.createServer(dto, 1))
            .thenThrow(new RuntimeException("Server creation failed"));

    // Act & Assert
    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> controller.createServer(dto, authUser));

    assertEquals("Server creation failed", ex.getMessage());
}


    @Test
    void getUserDatabases_validUser_returnsOkResponse() {
        // Arrange
        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        SendDatabasesDTO dto = new SendDatabasesDTO();
        when(databaseManagementService.getUserDatabases(1))
                .thenReturn(dto);

        // Act
        ResponseEntity<?> response = controller.getUserDatabases(authUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(databaseManagementService).getUserDatabases(1);
    }

    /*
     * --------------------------------------------------------
     * createServer
     * --------------------------------------------------------
     */
@Test
void createServer_success_returnsCreated() throws Exception {
    // Arrange
    CreateServerDTO dto = new CreateServerDTO();
    dto.setServerName("MyServer");

    AuthUser authUser = mock(AuthUser.class);
    when(authUser.userId()).thenReturn(1);

    CreateServerDTO returnedDTO = new CreateServerDTO();
    returnedDTO.setServerId(10);

    when(databaseManagementService.createServer(dto, 1)).thenReturn(returnedDTO);

    // Act
    ResponseEntity<CreateServerDTO> response = controller.createServer(dto, authUser);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(returnedDTO.getServerId(), response.getBody().getServerId());
}


    /*
     * --------------------------------------------------------
     * createMysqlContainer
     * --------------------------------------------------------
     */
    @Test
    void createMysqlContainer_success_returnsOk() throws BadRequestException {
        int databaseId = 50;
        InitiateDatabaseDTO dto = new InitiateDatabaseDTO();
        dto.setContainerId(databaseId);

        when(databaseManagementService.initiateDatabase(databaseId)).thenReturn(dto);

        ResponseEntity<?> response = controller.createMysqlContainer(databaseId);

        assertEquals(200, response.getStatusCode().value());
        assertSame(dto, response.getBody());
    }

@Test
void createMysqlContainer_serviceThrowsException_throwsRuntimeException() {
    int databaseId = 50;

    when(databaseManagementService.initiateDatabase(databaseId))
            .thenThrow(new RuntimeException("DB not found"));

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> controller.createMysqlContainer(databaseId));

    assertEquals("DB not found", ex.getMessage());
}


    /*
     * --------------------------------------------------------
     * getUserServers
     * --------------------------------------------------------
     */
    @Test
    void getUserServers_success_returnsList() {
        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        CreateServerDTO s1 = new CreateServerDTO();
        s1.setServerId(10);
        s1.setServerName("S1");

        CreateServerDTO s2 = new CreateServerDTO();
        s2.setServerId(20);
        s2.setServerName("S2");

        List<CreateServerDTO> servers = Arrays.asList(s1, s2);
        when(databaseManagementService.getUserServers(1)).thenReturn(servers);

        ResponseEntity<?> response = controller.getUserServers(authUser);

        assertEquals(200, response.getStatusCode().value());
        assertSame(servers, response.getBody());
    }
}
