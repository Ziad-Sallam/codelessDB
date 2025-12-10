package backend.config.collab;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PathVariableInterceptor implements HandshakeInterceptor {
	// Define the URI template to match the endpoint
	private static final UriTemplate URI_TEMPLATE = new UriTemplate("/ws/collab/{diagramId}");

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {

		// 1. Get the URI path from the HTTP request (the handshake request)
		String path = request.getURI().getPath();
		log.info("Getting the URI path from the HTTP request (the handshake request)");

		// 2. Match the URI path against the template to extract variables
		if (URI_TEMPLATE.matches(path)) {
			Map<String, String> pathVariables = URI_TEMPLATE.match(path);

			// 3. Extract the ID and store it in the WebSocketSession attributes map
			String diagramId = pathVariables.get("diagramId");
			attributes.put("diagramId", diagramId);

			log.info("Handshake successful. Document ID: " + diagramId);
			return true; // Continue with the handshake
		}

		// If the path doesn't match the expected pattern, reject the handshake
		log.error("Handshake failed. Path did not match template: " + path);
		return false;
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