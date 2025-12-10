package backend.collab;

import backend.collab.Room;
import backend.collab.services.RedisStreamService;
import backend.collab.services.RoomManager;
import backend.collab.services.SnapshotService;
import backend.collab.services.UpdateWriter;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class RoomManagerTest {

    @Mock
    private UpdateWriter updateWriter;

    @Mock
    private SnapshotService snapshotService;

    @Mock
    private RedisStreamService redisService;

    @InjectMocks
    private RoomManager roomManager;

    // Inject the internal activeRooms map using reflection
    private Map<String, Room> activeRooms;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        activeRooms = new ConcurrentHashMap<>();

        // inject into private field
        var field = RoomManager.class.getDeclaredField("activeRooms");
        field.setAccessible(true);
        field.set(roomManager, activeRooms);
    }

    /*
     * -----------------------------------------------------------------------
     * joinRoom()
     * ---------------------------------------------------------------------
     */

    @Test
    void testJoinRoom_createsRoomAndAddsSession() {
        WebSocketSession session = mock(WebSocketSession.class);

        roomManager.joinRoom("D1", session);

        assertTrue(activeRooms.containsKey("D1"));
        assertEquals(1, activeRooms.get("D1").getSessions().size());
        assertTrue(activeRooms.get("D1").getSessions().contains(session));
    }

    @Test
    void testJoinRoom_addsToExistingRoom() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);

        roomManager.joinRoom("RoomX", s1);
        roomManager.joinRoom("RoomX", s2);

        assertEquals(2, activeRooms.get("RoomX").getSessions().size());
    }

    /*
     * -----------------------------------------------------------------------
     * leaveRoom()
     * ---------------------------------------------------------------------
     */

    @Test
    void testLeaveRoom_removesSession() {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);

        roomManager.joinRoom("A", s1);
        roomManager.joinRoom("A", s2);

        roomManager.leaveRoom("A", s1);

        assertEquals(1, activeRooms.get("A").getSessions().size());
        assertFalse(activeRooms.get("A").getSessions().contains(s1));
    }

    @Test
    void testLeaveRoom_removesRoomWhenEmpty() {
        WebSocketSession s1 = mock(WebSocketSession.class);

        roomManager.joinRoom("ROOM10", s1);
        roomManager.leaveRoom("ROOM10", s1);

        assertFalse(activeRooms.containsKey("ROOM10"));
    }

    @Test
    void testLeaveRoom_nonExistingRoom() {
        WebSocketSession s = mock(WebSocketSession.class);

        assertDoesNotThrow(() -> roomManager.leaveRoom("DoesNotExist", s));
    }

    /*
     * -----------------------------------------------------------------------
     * sendUpdate()
     * ---------------------------------------------------------------------
     */

    @Test
    void testSendUpdate_noSessions() {
        roomManager.sendUpdate("abc", "x".getBytes(), "sender");

        // No room exists → nothing written
        verify(updateWriter, never()).submitWriteTask(any());
        verify(redisService, never()).addUpdate(any(), any());
    }

    @Test
    void testSendUpdate_writesToRedisAndBroadcasts() throws Exception {
        String diagramId = "D100";
        byte[] data = "hello".getBytes();

        WebSocketSession sender = mock(WebSocketSession.class);
        when(sender.getId()).thenReturn("SENDER");
        when(sender.isOpen()).thenReturn(true);

        WebSocketSession other1 = mock(WebSocketSession.class);
        when(other1.getId()).thenReturn("A");
        when(other1.isOpen()).thenReturn(true);

        WebSocketSession other2 = mock(WebSocketSession.class);
        when(other2.getId()).thenReturn("B");
        when(other2.isOpen()).thenReturn(true);

        roomManager.joinRoom(diagramId, sender);
        roomManager.joinRoom(diagramId, other1);
        roomManager.joinRoom(diagramId, other2);

        roomManager.sendUpdate(diagramId, data, "SENDER");

        // verify write task submitted
        verify(updateWriter, times(1)).submitWriteTask(any());

        // verify redis update
        verify(redisService, times(1)).addUpdate(diagramId, data);

        // verify broadcasted message to non-sender sessions
        verify(other1, times(1)).sendMessage(any(BinaryMessage.class));
        verify(other2, times(1)).sendMessage(any(BinaryMessage.class));
        verify(sender, never()).sendMessage(any());
    }

    @Test
    void testSendUpdate_skipsClosedSessions() throws Exception {
        String diagramId = "ROOM1";
        byte[] data = "abc".getBytes();

        WebSocketSession sender = mock(WebSocketSession.class);
        when(sender.getId()).thenReturn("sender");
        when(sender.isOpen()).thenReturn(true);

        WebSocketSession closed = mock(WebSocketSession.class);
        when(closed.getId()).thenReturn("closedUser");
        when(closed.isOpen()).thenReturn(false);

        roomManager.joinRoom(diagramId, sender);
        roomManager.joinRoom(diagramId, closed);

        roomManager.sendUpdate(diagramId, data, "sender");

        // Closed session → should NEVER receive messages
        verify(closed, never()).sendMessage(any());
    }

    @Test
    void testSendUpdate_handlesIOException() throws Exception {
        String diagramId = "OOM";
        byte[] data = "test".getBytes();

        WebSocketSession sender = mock(WebSocketSession.class);
        when(sender.getId()).thenReturn("S");
        when(sender.isOpen()).thenReturn(true);

        WebSocketSession faulty = mock(WebSocketSession.class);
        when(faulty.getId()).thenReturn("faulty");
        when(faulty.isOpen()).thenReturn(true);
        doThrow(new IOException("fail")).when(faulty).sendMessage(any());

        roomManager.joinRoom(diagramId, sender);
        roomManager.joinRoom(diagramId, faulty);

        assertDoesNotThrow(() -> roomManager.sendUpdate(diagramId, data, "S"));

        // Even failed sends still count as attempted
        verify(faulty, times(1)).sendMessage(any());
    }
}
