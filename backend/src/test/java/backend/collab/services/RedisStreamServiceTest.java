package backend.collab.services;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

@ExtendWith(MockitoExtension.class)
class RedisStreamServiceTest {

	@Mock
	private RedisTemplate<String, byte[]> redisTemplate;

	@Mock
	private StreamOperations<String, Object, Object> streamOperations;

	@InjectMocks
	private RedisStreamService service;

	@Test
	void testAddUpdate() {
		// Mock the opsForStream chain
		when(redisTemplate.opsForStream()).thenReturn(streamOperations);

		String diagramId = "test-d-id";
		byte[] update = new byte[] { 1, 2, 3 };

		service.addUpdate(diagramId, update);

		verify(streamOperations).add(any());
	}
}
