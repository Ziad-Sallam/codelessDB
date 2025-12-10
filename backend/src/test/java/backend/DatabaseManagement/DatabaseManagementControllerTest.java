package backend.DatabaseManagement;

import backend.databaseManagement.*;

import backend.security.AuthUser;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseManagementControllerTest {

    @InjectMocks
    private DatabaseManagementController controller;

    @Mock
    private DatabaseManagementService databaseManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /* --------------------------------------------------------
       createDatabase
     -------------------------------------------------------- */
    @Test
    void createDatabase_success_returnsOk() throws Exception {
        CreateDatabaseDTO dto = new CreateDatabaseDTO();
        dto.setDatabaseName("TestDB");
        dto.setDatabasePassword("pwd");

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        CreateDatabaseDTO returnedDTO = new CreateDatabaseDTO();
        returnedDTO.setDatabaseId(100);

        when(databaseManagementService.createDatabase(dto, 1)).thenReturn(returnedDTO);

        ResponseEntity<?> response = controller.createDatabase(dto, authUser);

        assertEquals(200, response.getStatusCode().value());
        assertSame(returnedDTO, response.getBody());
    }

    @Test
    void createDatabase_serviceThrowsException_returnsBadRequest() {
        CreateDatabaseDTO dto = new CreateDatabaseDTO();
        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        when(databaseManagementService.createDatabase(dto, 1))
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = controller.createDatabase(dto, authUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("DB error", body.get("error"));
    }

    @Test
    void createServer_serviceThrowsException_returnsBadRequest() {
        // Arrange
        CreateServerDTO dto = new CreateServerDTO();

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        when(databaseManagementService.createServer(dto, 1))
                .thenThrow(new RuntimeException("Server creation failed"));

        // Act
        ResponseEntity<?> response = controller.createServer(dto, authUser);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();

        assertNotNull(body);
        assertEquals("Server creation failed", body.get("error"));
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
        ResponseEntity<?> response = controller.getUserDAtabases(authUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());

        verify(databaseManagementService).getUserDatabases(1);
    }




    /* --------------------------------------------------------
       createServer
     -------------------------------------------------------- */
    @Test
    void createServer_success_returnsOk() throws Exception {
        CreateServerDTO dto = new CreateServerDTO();
        dto.setServerName("MyServer");

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        CreateServerDTO returnedDTO = new CreateServerDTO();
        returnedDTO.setServerId(10);

        when(databaseManagementService.createServer(dto, 1)).thenReturn(returnedDTO);

        ResponseEntity<?> response = controller.createServer(dto, authUser);

        assertEquals(200, response.getStatusCode().value());
        assertSame(returnedDTO, response.getBody());
    }

    /* --------------------------------------------------------
       createMysqlContainer
     -------------------------------------------------------- */
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
    void createMysqlContainer_serviceThrowsException_returnsInternalServerError() throws BadRequestException {
        int databaseId = 50;

        when(databaseManagementService.initiateDatabase(databaseId))
                .thenThrow(new RuntimeException("DB not found"));

        ResponseEntity<?> response = controller.createMysqlContainer(databaseId);

        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    /* --------------------------------------------------------
       getUserServers
     -------------------------------------------------------- */
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
