package backend.config.collab;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.UriComponentsBuilder; // New import for query parsing

import backend.entities.joins.UserDiagram;
import backend.security.AuthUser;
import backend.security.JwtExtractor;
import backend.user.Role;
import backend.userDiagramManagement.exceptions.DiagramException;
import backend.userDiagramManagement.service.UserDiagramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PathVariableInterceptor implements HandshakeInterceptor {

	private static final UriTemplate URI_TEMPLATE = new UriTemplate("/ws/collab/{diagramId}");

	private final JwtExtractor jwtExtractor;

	private final UserDiagramService diagramService;

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {

		String path = request.getURI().getPath();

		// --- 1. PATH VARIABLE EXTRACTION ---
		if (!URI_TEMPLATE.matches(path)) {
			log.error("Handshake failed. Path did not match template: {}", path);
			return false;
		}

		Map<String, String> pathVariables = URI_TEMPLATE.match(path);
		String diagramIdStr = pathVariables.get("diagramId");
		attributes.put("diagramId", diagramIdStr);

		// TOKEN EXTRACTION (FROM QUERY PARAMETER)
		String token = UriComponentsBuilder.fromUri(request.getURI())
													  .build()
													  .getQueryParams()
													  .getFirst("token");

		log.info("Extracted token from query parameter: {}", token == null ? "No" : "Yes");

		// AUTHENTICATION & AUTHORIZATION 
		try {
			AuthUser authUser = jwtExtractor.authenticate(token, false);

			if (authUser == null) {
				log.error("Handshake failed: Authorization token required or invalid.");
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				return false;
			}

			attributes.put("userId", authUser.userId());
			attributes.put("username", authUser.username());
			
			UUID diagramId = UUID.fromString(diagramIdStr);
			
			// AUTHORIZATION (Check diagram access)
			UserDiagram userDiagram = diagramService.getUserDiagramOrThrow(authUser.userId(), diagramId);
			Role role = userDiagram.getRole();
			attributes.put("role", role);

			log.info("Handshake successful. Diagram ID: {} | User: {}", diagramIdStr, authUser.username());

			return true;

		} catch (JwtValidationException ex) {
			log.warn("JWT validation failed during handshake for path {}: {}", path, ex.getMessage());
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return false;

		} catch (DiagramException.PermissionDeniedException e) {
			response.setStatusCode(HttpStatus.FORBIDDEN); // Use FORBIDDEN (403) for permission issues
			log.warn("User {} is forbidden from accessing diagram {}", attributes.get("username"), diagramIdStr);
			return false;

		} catch (IllegalArgumentException ex) {
			// Handles UUID.fromString() failure
			log.error("Invalid Diagram ID format: {}", diagramIdStr);
			return false;
		}
	}

	@Override
	public void afterHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Exception exception) {

		log.info("Handshake Established");
		// Optional: Perform cleanup or logging after the handshake
	}
}