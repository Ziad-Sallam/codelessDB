package backend.config.collab.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

class RedisConfigTest {

	@Test
	void testRedisConnectionFactory() {
		RedisConfig config = new RedisConfig();
		RedisConnectionFactory factory = config.redisConnectionFactory();
		assertNotNull(factory);
	}

	@Test
	void testStreamRedisTemplate() {
		RedisConfig config = new RedisConfig();
		RedisConnectionFactory factory = mock(RedisConnectionFactory.class);

		RedisTemplate<String, byte[]> template = config.streamRedisTemplate(factory);

		assertNotNull(template);
		assertNotNull(template.getConnectionFactory());

		@SuppressWarnings("unchecked")
		RedisSerializer<byte[]> valueSerializer = (RedisSerializer<byte[]>) template.getValueSerializer();
		assertNotNull(valueSerializer);

		byte[] input = new byte[] { 1, 2, 3 };
		byte[] serialized = valueSerializer.serialize(input);
		assertSame(input, serialized);

		byte[] deserialized = (byte[]) valueSerializer.deserialize(input);
		assertSame(input, deserialized);
	}
}
