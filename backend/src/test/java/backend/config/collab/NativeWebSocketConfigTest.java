package backend.config.collab;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import backend.collab.CollabWebSocketHandler;

@ExtendWith(MockitoExtension.class)
class NativeWebSocketConfigTest {

	@Mock
	private CollabWebSocketHandler handler;

	@Mock
	private CollabInterceptor interceptor;

	@InjectMocks
	private NativeWebSocketConfig config;

	@Test
	void testRegisterWebSocketHandlers() {
		WebSocketHandlerRegistry registry = mock(WebSocketHandlerRegistry.class);
		WebSocketHandlerRegistration registration = mock(WebSocketHandlerRegistration.class);

		when(registry.addHandler(eq(handler), eq("/ws/collab/*"))).thenReturn(registration);
		when(registration.setAllowedOrigins("*")).thenReturn(registration);

		config.registerWebSocketHandlers(registry);

		verify(registry).addHandler(handler, "/ws/collab/*");
		verify(registration).setAllowedOrigins("*");
		verify(registration).addInterceptors(interceptor);
	}
}
