package backend.collab.services;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

interface RedisDAO {

	void addUpdate(String diagramId, byte[] update);

	boolean diagramExists(String diagramId);
}

@Service
public class RedisStreamService implements RedisDAO {

	@Autowired
	private RedisTemplate<String, byte[]> redisTemplate;

	private static final String PREFIX_STREAM_KEY = "stream:";

	private String streamKey(String diagramId) {
		return PREFIX_STREAM_KEY + diagramId;
	}

	@Override
	public void addUpdate(String diagramId, byte[] update) {
		String key = streamKey(diagramId);
		
		Map<String, byte[]> map = Map.of("update", update);
		
		// The stream key is created automatically if it doesn't exist
		// XADD stream:{docId} * update=<bytes>
		redisTemplate.opsForStream().add(StreamRecords.newRecord().in(key).ofMap(map));
	}

	/**
	 * Checks if the stream key for a diagram exists in Redis.
	 */
	@Override
	public boolean diagramExists(String diagramId) {
		String key = streamKey(diagramId);
		Boolean exists = redisTemplate.hasKey(key);
		return Boolean.TRUE.equals(exists);
	}
}