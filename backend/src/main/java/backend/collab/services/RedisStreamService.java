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
		// byte[] trimmed;
		// if (update[0] == 0 && (update[1] == 1 || update[1] == 2)) {
		// 	trimmed = Arrays.copyOfRange(update, 2, update.length);
		// 	System.out.println("------------------");
		// 	System.out.println(Arrays.toString(
		// 	IntStream.range(0, trimmed.length)
		// 				.map(i -> Byte.toUnsignedInt(trimmed[i]))
		// 				.toArray()
		// 	));
		// 	System.out.println("------------------");

		// } else {
		// 	log.warn("Update should not be stored: {}", update);
		// 	return;
		// }

		String key = streamKey(diagramId);

		Map<String, byte[]> map = Map.of("update", update);

		// The stream key is created automatically if it doesn't exist
		// XADD stream:{docId} * update=<bytes>
		redisTemplate.opsForStream().add(StreamRecords.newRecord().in(key).ofMap(map));
	}
}