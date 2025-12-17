package backend.agent;

import backend.agent.WebSocketHandler.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnlineUserTrackerTest {

    private OnlineUserTracker onlineUserTracker;

    @BeforeEach
    void setUp() {
        onlineUserTracker = new OnlineUserTracker();
    }

    /*
     * --------------------------------------------------------
     * addUser
     * --------------------------------------------------------
     */

    @Test
    void addUser_userIsOnline() {
        onlineUserTracker.addUser("alice");

        assertTrue(onlineUserTracker.isOnline("alice"));
    }

    @Test
    void addUser_duplicateUser_onlyOneInstance() {
        onlineUserTracker.addUser("bob");
        onlineUserTracker.addUser("bob");

        Set<String> users = onlineUserTracker.getOnlineUsers();

        assertEquals(1, users.size());
        assertTrue(users.contains("bob"));
    }

    /*
     * --------------------------------------------------------
     * removeUser
     * --------------------------------------------------------
     */

    @Test
    void removeUser_userIsRemoved() {
        onlineUserTracker.addUser("charlie");
        onlineUserTracker.removeUser("charlie");

        assertFalse(onlineUserTracker.isOnline("charlie"));
    }

    @Test
    void removeUser_nonExistingUser_noException() {
        assertDoesNotThrow(() -> onlineUserTracker.removeUser("ghost"));
    }

    /*
     * --------------------------------------------------------
     * isOnline
     * --------------------------------------------------------
     */

    @Test
    void isOnline_returnsFalseForOfflineUser() {
        assertFalse(onlineUserTracker.isOnline("david"));
    }

    /*
     * --------------------------------------------------------
     * getOnlineUsers
     * --------------------------------------------------------
     */

    @Test
    void getOnlineUsers_returnsUnmodifiableSet() {
        onlineUserTracker.addUser("eve");

        Set<String> users = onlineUserTracker.getOnlineUsers();

        assertThrows(UnsupportedOperationException.class,
                () -> users.add("mallory"));
    }

    @Test
    void getOnlineUsers_reflectsCurrentUsers() {
        onlineUserTracker.addUser("alice");
        onlineUserTracker.addUser("bob");

        Set<String> users = onlineUserTracker.getOnlineUsers();

        assertEquals(2, users.size());
        assertTrue(users.contains("alice"));
        assertTrue(users.contains("bob"));
    }
}
