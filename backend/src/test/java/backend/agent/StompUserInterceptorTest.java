package backend.agent;

import backend.agent.WebSocketHandler.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

class StompUserInterceptorTest {

    @InjectMocks
    private StompUserInterceptor interceptor;

    @Mock
    private OnlineUserTracker tracker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /*
     * --------------------------------------------------------
     * CONNECT with Authorization header
     * --------------------------------------------------------
     */
    @Test
    void preSend_connectWithAuthorization_setsUserAndTracks() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "user123");

        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(message, mock(MessageChannel.class));

        assertEquals("user123", accessor.getUser().getName());
        verify(tracker, times(1)).addUser("user123");
    }

    /*
     * --------------------------------------------------------
     * CONNECT without Authorization header → anonymous user
     * --------------------------------------------------------
     */
    @Test
    void preSend_connectWithoutAuthorization_generatesAnonUser() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);

        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(message, mock(MessageChannel.class));

        String userName = accessor.getUser().getName();
        assertNotNull(userName);
        assertTrue(userName.startsWith("anon-"));

        verify(tracker, times(1)).addUser(userName);
    }

    /*
     * --------------------------------------------------------
     * DISCONNECT removes user from tracker
     * --------------------------------------------------------
     */
    @Test
    void preSend_disconnect_removesUserFromTracker() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setUser(() -> "user456");

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(message, mock(MessageChannel.class));

        verify(tracker, times(1)).removeUser("user456");
    }

    /*
     * --------------------------------------------------------
     * Other STOMP commands → no side effects
     * --------------------------------------------------------
     */
    @Test
    void preSend_otherCommand_doesNothing() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setUser(() -> "user789");

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertSame(message, result);
        verifyNoInteractions(tracker);
    }
}
