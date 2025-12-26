package backend.config.collab;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.web.socket.WebSocketHandler;

import backend.entities.joins.UserDiagram;
import backend.security.AuthUser;
import backend.security.JwtExtractor;
import backend.user.Role;
import backend.userDiagramManagement.exceptions.DiagramException;
import backend.userDiagramManagement.service.UserDiagramService;

@ExtendWith(MockitoExtension.class)
class CollabInterceptorTest {

	@Mock
	private JwtExtractor jwtExtractor;

	@Mock
	private UserDiagramService diagramService;

	@InjectMocks
	private CollabInterceptor interceptor;

	private ServerHttpRequest request;
	private ServerHttpResponse response;
	private WebSocketHandler wsHandler;
	private Map<String, Object> attributes;
	private String validDiagramId = "550e8400-e29b-41d4-a716-446655440000";
	private URI validUri;

	@BeforeEach
	void setUp() throws Exception {
		request = mock(ServerHttpRequest.class);
		response = mock(ServerHttpResponse.class);
		wsHandler = mock(WebSocketHandler.class);
		attributes = new HashMap<>();
		validUri = new URI("/ws/collab/" + validDiagramId + "?token=valid-token");
	}

	@Test
	void testBeforeHandshake_Success() throws Exception {
		when(request.getURI()).thenReturn(validUri);

		AuthUser authUser = new AuthUser(123, "testuser");
		when(jwtExtractor.authenticate(eq("valid-token"), anyBoolean())).thenReturn(authUser);

		UserDiagram userDiagram = mock(UserDiagram.class);
		when(userDiagram.getRole()).thenReturn(Role.WRITER);

		when(diagramService.getUserDiagramOrThrow(eq(123), any(UUID.class)))
				.thenReturn(userDiagram);

		boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

		assertTrue(result);
		assertEquals(validDiagramId, attributes.get("diagramId"));
		assertEquals(123, attributes.get("userId"));
		assertEquals("testuser", attributes.get("username"));
		assertEquals(Role.WRITER, attributes.get("role"));
	}

	@Test
	void testBeforeHandshake_InvalidPath() throws Exception {
		URI invalidUri = new URI("/ws/other/something");
		when(request.getURI()).thenReturn(invalidUri);

		boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

		assertFalse(result);
	}

	@Test
	void testBeforeHandshake_AuthFailed_TokenInvalid() throws Exception {
		URI uri = new URI("/ws/collab/" + validDiagramId + "?token=invalid-token");
		when(request.getURI()).thenReturn(uri);
		when(jwtExtractor.authenticate(eq("invalid-token"), anyBoolean())).thenReturn(null);

		boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

		assertFalse(result);
		verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void testBeforeHandshake_AuthThrowException() throws Exception {
		URI uri = new URI("/ws/collab/" + validDiagramId + "?token=error-token");
		when(request.getURI()).thenReturn(uri);

		OAuth2Error error = new OAuth2Error("invalid_token", "Invalid token", null);
		when(jwtExtractor.authenticate(eq("error-token"), anyBoolean()))
				.thenThrow(new JwtValidationException("Invalid token", java.util.List.of(error)));

		boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

		assertFalse(result);
		verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void testBeforeHandshake_PermissionDenied() throws Exception {
		when(request.getURI()).thenReturn(validUri);

		AuthUser authUser = new AuthUser(123, "testuser");
		when(jwtExtractor.authenticate(anyString(), anyBoolean())).thenReturn(authUser);

		when(diagramService.getUserDiagramOrThrow(anyInt(), any(UUID.class)))
				.thenThrow(new DiagramException.PermissionDeniedException("Denied"));

		boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

		assertFalse(result);
		verify(response).setStatusCode(HttpStatus.FORBIDDEN);
	}

	@Test
	void testBeforeHandshake_InvalidUUID() throws Exception {
		URI badUuidUri = new URI("/ws/collab/bad-uuid?token=valid-token");
		when(request.getURI()).thenReturn(badUuidUri);

		AuthUser authUser = new AuthUser(123, "testuser");
		when(jwtExtractor.authenticate(anyString(), anyBoolean())).thenReturn(authUser);

		boolean result = interceptor.beforeHandshake(request, response, wsHandler, attributes);

		assertFalse(result);
	}

	@Test
	void testAfterHandshake() {
		interceptor.afterHandshake(request, response, wsHandler, null);
	}
}
