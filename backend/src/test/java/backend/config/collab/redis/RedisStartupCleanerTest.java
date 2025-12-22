package backend.config.collab.redis;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

class RedisStartupCleanerTest {

	@SuppressWarnings("deprecation")
	@Test
	void testClearRedisOnStartup() {
		RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
		RedisConnection connection = mock(RedisConnection.class);

		when(factory.getConnection()).thenReturn(connection);

		RedisStartupCleaner cleaner = new RedisStartupCleaner(factory);
		cleaner.clearRedisOnStartup();

		verify(connection).flushAll();
		verify(connection).close();
	}
}
