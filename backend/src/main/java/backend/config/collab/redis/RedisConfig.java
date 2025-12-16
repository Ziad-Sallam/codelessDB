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

		// template.setValueSerializer(RedisSerializer.byteArray());
		// template.setHashValueSerializer(RedisSerializer.byteArray());

		RedisSerializer<byte[]> rawByteSerializer = new ByteArraySerializer();
    	template.setValueSerializer(rawByteSerializer);
    	template.setHashValueSerializer(rawByteSerializer);

		// RedisSerializer<byte[]> base64Serializer = new Base64StringSerializer();
		// template.setValueSerializer(base64Serializer);
		// template.setHashValueSerializer(base64Serializer);

		template.afterPropertiesSet();
		return template;
	}

}