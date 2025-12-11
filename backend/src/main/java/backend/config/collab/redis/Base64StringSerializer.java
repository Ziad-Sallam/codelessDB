package backend.config.collab.redis;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class Base64StringSerializer implements RedisSerializer<byte[]> {

	@Override
	public byte[] serialize(byte[] bytes) throws SerializationException {
		if (bytes == null) return null;
		String base64 = Base64.getEncoder().encodeToString(bytes);
		return base64.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public byte[] deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null) return null;
		String base64 = new String(bytes, StandardCharsets.UTF_8);
		return Base64.getDecoder().decode(base64);
	}

}
