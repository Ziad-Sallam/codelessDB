package backend.agent;

import backend.agent.HTTPHandler.*;

import backend.agent.WebSocketHandler.AgentMessageDTO;
import backend.agent.WebSocketHandler.ClientResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import backend.security.AuthUser;

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

    @Test
    void sendToUser_success_returnsOk() throws Exception {
        MessageDTO request = new MessageDTO();
        request.setContent("SELECT * FROM users");
        request.setDatabaseId(10);

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        ClientResponseDTO expectedResponse = new ClientResponseDTO("corr-1", true, "OK");
        when(messageService.runQuery(eq(10), any(AgentMessageDTO.class), eq(1)))
                .thenReturn(expectedResponse);

        ResponseEntity<?> responseEntity = controller.sendToUser(request, authUser);

        assertEquals(200, responseEntity.getStatusCodeValue());
        assertSame(expectedResponse, responseEntity.getBody());

        // Verify AgentMessageDTO sender is "Server"
        ArgumentCaptor<AgentMessageDTO> captor = ArgumentCaptor.forClass(AgentMessageDTO.class);
        verify(messageService).runQuery(eq(10), captor.capture(), eq(1));
        assertEquals("Server", captor.getValue().getSender());
        assertEquals("SELECT * FROM users", captor.getValue().getContent());
    }

    @Test
    void sendToUser_exception_returnsBadRequest() throws Exception {
        MessageDTO request = new MessageDTO();
        request.setContent("SELECT * FROM users");
        request.setDatabaseId(10);

        AuthUser authUser = mock(AuthUser.class);
        when(authUser.userId()).thenReturn(1);

        when(messageService.runQuery(eq(10), any(AgentMessageDTO.class), eq(1)))
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> responseEntity = controller.sendToUser(request, authUser);

        assertEquals(400, responseEntity.getStatusCodeValue());
        assertEquals("DB error", responseEntity.getBody());
    }
}
