package backend.agent;

import backend.agent.WebSocketHandler.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

class UserHandshakeInterceptorTest {

    private UserHandshakeInterceptor interceptor;

    private ServerHttpRequest request;
    private ServerHttpResponse response;
    private WebSocketHandler webSocketHandler;

    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        interceptor = new UserHandshakeInterceptor();

        request = mock(ServerHttpRequest.class);
        response = mock(ServerHttpResponse.class);
        webSocketHandler = mock(WebSocketHandler.class);

        attributes = new HashMap<>();
    }

    /* --------------------------------------------------------
       beforeHandshake
     -------------------------------------------------------- */

    @Test
    void beforeHandshake_withAuthorizationHeader_setsUserId() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "user-123");

        when(request.getHeaders()).thenReturn(headers);

        boolean result = interceptor.beforeHandshake(
                request, response, webSocketHandler, attributes);

        assertTrue(result);
        assertEquals("user-123", attributes.get("userId"));
    }

    @Test
    void beforeHandshake_withoutAuthorizationHeader_setsAnonymousUser() {
        HttpHeaders headers = new HttpHeaders();

        when(request.getHeaders()).thenReturn(headers);

        boolean result = interceptor.beforeHandshake(
                request, response, webSocketHandler, attributes);

        assertTrue(result);

        assertTrue(attributes.containsKey("userId"));
        assertTrue(attributes.get("userId").toString().startsWith("anon-"));
    }

    /* --------------------------------------------------------
       afterHandshake
     -------------------------------------------------------- */

    @Test
    void afterHandshake_doesNothing_andDoesNotThrow() {
        assertDoesNotThrow(() ->
                interceptor.afterHandshake(
                        request,
                        response,
                        webSocketHandler,
                        null
                ));
    }
}
