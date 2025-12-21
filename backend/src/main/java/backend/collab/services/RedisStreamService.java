package backend.collab.services;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisStreamService {

	@Autowired
	private RedisTemplate<String, byte[]> redisTemplate;

	private static final String PREFIX_STREAM_KEY = "stream:";

	private String streamKey(String diagramId) {
		return PREFIX_STREAM_KEY + diagramId;
	}

	public void addUpdate(String diagramId, byte[] update) {
		String key = streamKey(diagramId);

		Map<String, byte[]> map = Map.of("update", update);

		// The stream key is created automatically if it doesn't exist
		// XADD stream:{docId} * update=<bytes>
		redisTemplate.opsForStream().add(StreamRecords.newRecord().in(key).ofMap(map));
	}
}