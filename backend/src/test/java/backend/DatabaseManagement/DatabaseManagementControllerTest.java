package backend.databaseManagement;

import java.util.Arrays;
import java.util.List;

import org.apache.coyote.BadRequestException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import backend.databaseManagement.dto.AddDatabaseToUser;
import backend.databaseManagement.dto.CreateDatabaseDTO;
import backend.databaseManagement.dto.CreateServerDTO;
import backend.databaseManagement.dto.DatabaseUserDto;
import backend.databaseManagement.dto.InitiateDatabaseDTO;
import backend.databaseManagement.dto.PasswordRequest;
import backend.databaseManagement.dto.SendDatabasesDTO;
import backend.entities.joins.UserDatabaseAccess;
import backend.security.AuthUser;
import backend.entities.User;
import backend.user.Role;

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

    /*
     * --------------------------------------------------------
     * addDatabaseToUser
     * --------------------------------------------------------
     */
    @Test
    void addDatabaseToUser_success_returnsNoContent() {
        // Arrange
        AddDatabaseToUser dto = new AddDatabaseToUser();
        dto.setDatabaseId(10);
        dto.setUserId(5);
        dto.setRole("READ");

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        doNothing().when(databaseManagementService)
                .addDatabaseToUser(10, 5, 1, "READ");

        // Act
        ResponseEntity<AddDatabaseToUser> response = controller.addDatabaseToUser(dto, authUser);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(databaseManagementService, times(1))
                .addDatabaseToUser(10, 5, 1, "READ");
    }

    /*
     * --------------------------------------------------------
     * removeDatabaseFromUser
     * --------------------------------------------------------
     */
    @Test
    void removeDatabaseFromUser_success_returnsNoContent() {
        // Arrange
        AddDatabaseToUser dto = new AddDatabaseToUser();
        dto.setDatabaseId(20);
        dto.setUserId(6);

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(2);

        doNothing().when(databaseManagementService)
                .removeDatabaseFromUser(20, 6, 2);

        // Act
        ResponseEntity<Void> response = controller.removeDatabaseFromUser(dto, authUser);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(databaseManagementService, times(1))
                .removeDatabaseFromUser(20, 6, 2);
    }

    /*
     * --------------------------------------------------------
     * checkDatabasePassword
     * --------------------------------------------------------
     */
    @Test
    void checkDatabasePassword_correctPassword_returnsTrue() {
        // Arrange
        PasswordRequest request = new PasswordRequest();
        request.setDatabaseId(30);
        request.setPassword("secret");

        when(databaseManagementService.checkDatabasePassword(30, "secret"))
                .thenReturn(true);

        // Act
        ResponseEntity<Boolean> response = controller.checkDatabasePassword(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());

        verify(databaseManagementService, times(1))
                .checkDatabasePassword(30, "secret");
    }

    @Test
    void checkDatabasePassword_wrongPassword_returnsFalse() {
        // Arrange
        PasswordRequest request = new PasswordRequest();
        request.setDatabaseId(30);
        request.setPassword("wrong");

        when(databaseManagementService.checkDatabasePassword(30, "wrong"))
                .thenReturn(false);

        // Act
        ResponseEntity<Boolean> response = controller.checkDatabasePassword(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
    }

@Test
void updateUserRole_success_returnsNoContent() {
    // Arrange
    AddDatabaseToUser request = new AddDatabaseToUser();
    request.setDatabaseId(10);
    request.setUserId(2);
    request.setRole("WRITER");

    AuthUser authUser = mock(AuthUser.class);
    when(authUser.userId()).thenReturn(1);

    doNothing().when(databaseManagementService).updateUserRole(
            10, 2, 1, "WRITER"
    );

    // Act
    ResponseEntity<Void> response =
            controller.updateUserRole(request, authUser);

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNull(response.getBody());

    verify(databaseManagementService).updateUserRole(
            10, 2, 1, "WRITER"
    );
}

@Test
void getDatabaseUsers_success_returnsOkAndUsers() {
    // Arrange
    int databaseId = 10;

    AuthUser authUser = mock(AuthUser.class);
    when(authUser.userId()).thenReturn(1);

    // Mock User
    User user1 = mock(User.class);
    when(user1.getId()).thenReturn(2);
    when(user1.getUsername()).thenReturn("user1");
    when(user1.getEmail()).thenReturn("user1@test.com");
    when(user1.getPicture()).thenReturn("pic1.png");

    // Mock UserDatabaseAccess
    UserDatabaseAccess access1 = mock(UserDatabaseAccess.class);
    when(access1.getUser()).thenReturn(user1);
    when(access1.getRole()).thenReturn(Role.READER);

    DatabaseUserDto dto1 = new DatabaseUserDto(access1);

    List<DatabaseUserDto> users = List.of(dto1);

    when(databaseManagementService.getDatabaseUsers(databaseId, 1))
            .thenReturn(users);

    // Act
    ResponseEntity<List<DatabaseUserDto>> response =
            controller.getDatabaseUsers(databaseId, authUser);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());

    DatabaseUserDto result = response.getBody().get(0);
    assertEquals(2, result.getUserId());
    assertEquals("user1", result.getUsername());
    assertEquals("user1@test.com", result.getEmail());
    assertEquals("pic1.png", result.getPicture());
    assertEquals("READER", result.getRole());

    verify(databaseManagementService)
            .getDatabaseUsers(databaseId, 1);
}


}
