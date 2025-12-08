package backend.config.collab.redis;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class UUIDSerializer implements RedisSerializer<UUID> {

	@Override
	public byte[] serialize(UUID uuid) throws SerializationException {
		if (uuid == null)
			return new byte[0];

		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		return buffer.array();
	}

	@Override
	public UUID deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null || bytes.length != 16)
			return null;
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		long most = buffer.getLong();
		long least = buffer.getLong();
		return new UUID(most, least);
	}
}