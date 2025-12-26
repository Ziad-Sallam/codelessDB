package backend.agent;

import backend.agent.HTTPHandler.*;
import backend.agent.WebSocketHandler.AgentMessageDTO;
import backend.agent.WebSocketHandler.ClientResponseDTO;
import backend.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageControllerTest {

    @InjectMocks
    private MessageController controller;

    @Mock
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ================= sendToUser =================

    @Test
    void sendToUser_success_returnsOk() {
        MessageDTO request = new MessageDTO();
        request.setContent("SELECT * FROM users");
        request.setDatabaseId(10);

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        ClientResponseDTO expectedResponse =
                new ClientResponseDTO("corr-1", true, "OK");

        when(messageService.runQuery(eq(10), any(AgentMessageDTO.class), eq(1)))
                .thenReturn(expectedResponse);

        ResponseEntity<ClientResponseDTO> response =
                controller.sendToUser(request, authUser);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expectedResponse, response.getBody());

        ArgumentCaptor<AgentMessageDTO> captor =
                ArgumentCaptor.forClass(AgentMessageDTO.class);

        verify(messageService).runQuery(eq(10), captor.capture(), eq(1));
        assertEquals("Server", captor.getValue().getSender());
        assertEquals("SELECT * FROM users", captor.getValue().getContent());
    }

    @Test
    void sendToUser_exception_isThrown() {
        MessageDTO request = new MessageDTO();
        request.setContent("SELECT * FROM users");
        request.setDatabaseId(10);

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        when(messageService.runQuery(eq(10), any(AgentMessageDTO.class), eq(1)))
                .thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.sendToUser(request, authUser)
        );

        assertEquals("DB error", ex.getMessage());
    }

    // ================= isDatabaseOnline =================

    @Test
    void isDatabaseOnline_success_returnsOk() {
        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        when(messageService.databaseIsOnline(1, 10)).thenReturn(true);

        ResponseEntity<Boolean> response =
                controller.isDatabaseOnline(10, authUser);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody());
    }

    @Test
    void isDatabaseOnline_exception_isThrown() {
        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        when(messageService.databaseIsOnline(1, 10))
                .thenThrow(new RuntimeException("Unauthorized"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> controller.isDatabaseOnline(10, authUser)
        );

        assertEquals("Unauthorized", ex.getMessage());
    }


}
