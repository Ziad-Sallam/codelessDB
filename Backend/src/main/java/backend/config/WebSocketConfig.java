package backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import backend.agent.WebSocketHandler.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic"); // Broker prefix for outgoing messages
		registry.setApplicationDestinationPrefixes("/app"); // Prefix for incoming messages
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/agent-ws")
				.setAllowedOrigins("*")
				.addInterceptors(new UserHandshakeInterceptor());
		// .withSockJS(); // WebSocket endpoint
	}
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new StompUserInterceptor());	
	}

}