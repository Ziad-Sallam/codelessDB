package backend.agent;

import backend.agent.WebSocketHandler.AgentController;
import backend.agent.WebSocketHandler.AgentMessageDTO;
import backend.agent.WebSocketHandler.ClientResponseDTO;
import backend.agent.WebSocketHandler.OnlineUserTracker;
import backend.databaseManagement.UserDatabaseRepository;
import backend.databaseManagement.exception.DatabaseException.DatabaseNotConnectedException;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.databaseManagement.exception.DatabaseException.DatabaseNotFoundException;
import backend.agent.HTTPHandler.MessageService;
import backend.entities.User;
import backend.entities.UserDatabase;
import backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MessageServiceTest {

    private UserRepository userRepository;
    private UserDatabaseRepository userDatabaseRepository;
    private OnlineUserTracker tracker;
    private AgentController agentController;

    private MessageService messageService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        userDatabaseRepository = mock(UserDatabaseRepository.class);
        tracker = mock(OnlineUserTracker.class);
        agentController = mock(AgentController.class);

        messageService = new MessageService(tracker, agentController, userDatabaseRepository, userRepository);

    }

    @Test
    void testUserNotFound() {
        when(userRepository.findById(10)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> messageService.runQuery(5, new AgentMessageDTO(), 10));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testDatabaseNotConnected() {
        User mockUser = new User();
        UserDatabase mockDB = new UserDatabase();
        mockUser.setAccessibleDatabases(Collections.singleton(mockDB));

        when(userRepository.findById(10)).thenReturn(mockUser);
        when(userDatabaseRepository.findById(5)).thenReturn(Optional.of(mockDB));
        when(tracker.isOnline("5")).thenReturn(false);

        DatabaseNotConnectedException ex = assertThrows(DatabaseNotConnectedException.class,
                () -> messageService.runQuery(5, new AgentMessageDTO(), 10));

        assertEquals("Database not connected", ex.getMessage());
    }

    @Test
    void testDatabaseNotFound() {
        User mockUser = new User();
        mockUser.setAccessibleDatabases(Collections.emptySet());

        when(userRepository.findById(10)).thenReturn(mockUser);
        when(tracker.getOnlineUsers()).thenReturn(
                Collections.singleton("5"));

        when(userDatabaseRepository.findById(5)).thenReturn(Optional.empty());

        DatabaseNotFoundException ex = assertThrows(DatabaseNotFoundException.class,
                () -> messageService.runQuery(5, new AgentMessageDTO(), 10));

        assertEquals("Database not found", ex.getMessage());
    }

    @Test
    void testUnauthorizedAccess() {
        User mockUser = new User();
        mockUser.setAccessibleDatabases(Collections.emptySet());

        when(userRepository.findById(10)).thenReturn(mockUser);
        when(tracker.getOnlineUsers()).thenReturn(Collections.singleton("5"));

        UserDatabase db = new UserDatabase();
        when(userDatabaseRepository.findById(5)).thenReturn(Optional.of(db));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> messageService.runQuery(5, new AgentMessageDTO(), 10));

        assertEquals("Unauthorized access", ex.getMessage());
    }

@Test
void testSuccessfulQuery() throws Exception {
    // Mock user and database
    User mockUser = new User();
    UserDatabase mockDB = new UserDatabase();
    mockUser.setAccessibleDatabases(Collections.singleton(mockDB));

    when(userRepository.findById(10)).thenReturn(mockUser);
    when(userDatabaseRepository.findById(5)).thenReturn(Optional.of(mockDB));

    // Mock database is online
    when(tracker.isOnline("5")).thenReturn(true);

    // Mock agent response
    ClientResponseDTO mockResponse = new ClientResponseDTO();
    when(agentController.sendToUser(eq(5), any())).thenReturn(mockResponse);

    // Call the service
    ClientResponseDTO result = messageService.runQuery(5, new AgentMessageDTO(), 10);

    // Assertions
    assertNotNull(result);
    assertEquals(mockResponse, result);

    // Verify interaction with agentController
    verify(agentController, times(1)).sendToUser(eq(5), any());
}


    @Test
    void testAgentControllerThrowsException() throws Exception {

        User mockUser = new User();
        UserDatabase mockDB = new UserDatabase();
        mockUser.setAccessibleDatabases(Collections.singleton(mockDB));

        when(userRepository.findById(10)).thenReturn(mockUser);
        when(tracker.getOnlineUsers()).thenReturn(Collections.singleton("5"));
        when(userDatabaseRepository.findById(5)).thenReturn(Optional.of(mockDB));

        when(agentController.sendToUser(eq(5), any()))
                .thenThrow(new Exception("WS Error"));

        DatabaseNotConnectedException ex = assertThrows(DatabaseNotConnectedException.class,
                () -> messageService.runQuery(5, new AgentMessageDTO(), 10));

        assertEquals("Database not connected", ex.getMessage());
    }

    @Test
    void testDatabaseIsOnline_UnauthorizedAccess() {
        User user = new User();
        user.setAccessibleDatabases(Collections.emptySet());

        UserDatabase db = new UserDatabase();
        when(userRepository.findById(1)).thenReturn(user);
        when(userDatabaseRepository.findById(10)).thenReturn(Optional.of(db));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> messageService.databaseIsOnline(1, 10));
        assertEquals("Unauthorized access", ex.getMessage());
    }

    @Test
    void testDatabaseIsOnline_Online() {
        UserDatabase db = new UserDatabase();
        User user = new User();
        user.setAccessibleDatabases(Collections.singleton(db));

        when(userRepository.findById(1)).thenReturn(user);
        when(userDatabaseRepository.findById(10)).thenReturn(Optional.of(db));
        when(tracker.isOnline("10")).thenReturn(true);

        Boolean result = messageService.databaseIsOnline(1, 10);
        assertTrue(result);
    }

    @Test
    void testDatabaseIsOnline_Offline() {
        UserDatabase db = new UserDatabase();
        User user = new User();
        user.setAccessibleDatabases(Collections.singleton(db));

        when(userRepository.findById(1)).thenReturn(user);
        when(userDatabaseRepository.findById(10)).thenReturn(Optional.of(db));
        when(tracker.isOnline("10")).thenReturn(false);

        Boolean result = messageService.databaseIsOnline(1, 10);
        assertFalse(result);
    }

    @Test
    void testDatabaseIsOnline_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> messageService.databaseIsOnline(1, 10));
    }

}
