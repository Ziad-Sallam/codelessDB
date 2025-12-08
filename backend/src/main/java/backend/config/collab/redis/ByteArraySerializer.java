package backend.config.collab.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class ByteArraySerializer implements RedisSerializer<byte[]> {

	@Override
	public byte[] serialize(byte[] bytes) throws SerializationException {
		return bytes;
	}

	@Override
	public byte[] deserialize(byte[] bytes) throws SerializationException {
		return bytes;
	}
}