package backend.config.collab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import backend.collab.CollabWebSocketHandler;

@Configuration
@EnableWebSocket
public class NativeWebSocketConfig implements WebSocketConfigurer {

	@Autowired
	private CollabWebSocketHandler collabWebSocketHandler;

	@Autowired
	private PathVariableInterceptor pathVariableInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// Register your low-level handler at a distinct endpoint
		registry .addHandler(collabWebSocketHandler, "/ws/collab/*")
					.setAllowedOrigins("*")
					.addInterceptors(pathVariableInterceptor);
	}
}