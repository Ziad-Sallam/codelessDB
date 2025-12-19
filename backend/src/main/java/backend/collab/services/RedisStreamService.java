package backend.collab.services;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

interface RedisDAO {

	void addUpdate(String diagramId, byte[] update);

	List<byte[]> getAllUpdates(String diagramId);

	void deleteUntil(String diagramId, String lastRecordId);

	boolean diagramExists(String diagramId);

	void removeDiagramHistory(String diagramId);
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

	@Override
	public List<byte[]> getAllUpdates(String diagramId) {
		String key = streamKey(diagramId);

		if (!diagramExists(diagramId)) {
			return Collections.emptyList();
		}

		List<MapRecord<String, Object, Object>> records = 
			redisTemplate.opsForStream().range(key, Range.unbounded());

		List<byte[]> updates = new ArrayList<>();

		if (records != null) {
			for (MapRecord<String, Object, Object> rec : records) {
				Map<Object, Object> valueMap = rec.getValue();
				if (valueMap.containsKey("update") && valueMap.get("update") instanceof byte[]) {
					updates.add((byte[]) valueMap.get("update"));
				}
			}
		}

		return updates;
	}

	@Override
	public void deleteUntil(String diagramId, String lastRecordIdStr) {
		String key = streamKey(diagramId);

		if (!diagramExists(diagramId))
			return;

		RecordId lastRecordId = RecordId.of(lastRecordIdStr);

		// Read all IDs up to the specified record ID (inclusive)
		List<MapRecord<String, Object, Object>> toDelete = redisTemplate.opsForStream()
				.range(key, Range.closed("0-0", lastRecordId.toString()));

		if (toDelete == null || toDelete.isEmpty())
			return;

		List<RecordId> deleteIds = new ArrayList<>();
		for (MapRecord<String, Object, Object> record : toDelete) {
			deleteIds.add(record.getId());
		}

		if (!deleteIds.isEmpty()) {
			// XDEL key id1 id2 id3...
			redisTemplate.opsForStream().delete(key, deleteIds.toArray(new RecordId[0]));
		}
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

	/**
	 * Deletes the entire Redis Stream history for a diagram.
	 */
	@Override
	public void removeDiagramHistory(String diagramId) {
		String key = streamKey(diagramId);
		redisTemplate.delete(key);
	}
}