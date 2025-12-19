package backend.config.collab.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		// Uses Lettuce under the hood
		return new LettuceConnectionFactory();
	}

	@Bean
	public RedisTemplate<String, byte[]> streamRedisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, byte[]> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());

		// Values stored in stream map should be raw bytes
		RedisSerializer<byte[]> rawByteSerializer = new RedisSerializer<>() {
			@Override
			public byte[] serialize(byte[] bytes) {
				return bytes; // no conversion
			}

			@Override
			public byte[] deserialize(byte[] bytes) {
				return bytes; // no conversion
			}
		};

		template.setValueSerializer(rawByteSerializer);
		template.setHashValueSerializer(rawByteSerializer);

		template.afterPropertiesSet();
		return template;
	}

}