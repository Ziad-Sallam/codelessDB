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

    void addDiagram(String diagramId);

    void removeDiagram(String diagramId);
}

@Service
public class RedisStreamService implements RedisDAO {

    @Autowired
    private RedisTemplate<String, byte[]> redisTemplate;

    private static final String PREFIX_STREAM = "yjs:stream:";
    private static final String PREFIX_SET = "yjs:diagrams";

    private String streamKey(String diagramId) {
        return PREFIX_STREAM + diagramId;
    }

    @Override
    public void addDiagram(String diagramId) {
        redisTemplate.opsForSet().add(PREFIX_SET, diagramId.getBytes());
    }

    @Override
    public void removeDiagram(String diagramId) {
        redisTemplate.opsForSet().remove(PREFIX_SET, diagramId.getBytes());
        redisTemplate.delete(streamKey(diagramId));
    }

    @Override
    public void addUpdate(String diagramId, byte[] update) {
        String key = streamKey(diagramId);

        Map<String, byte[]> map = new HashMap<>();
        map.put("update", update);

        // XADD stream:yjs:{docId} * update=<bytes>
        redisTemplate.opsForStream().add(StreamRecords.newRecord().in(key).ofMap(map));
    }

    @Override
    public List<byte[]> getAllUpdates(String diagramId) {
        String key = streamKey(diagramId);

        List<MapRecord<String,Object,Object>> records = 
            redisTemplate.opsForStream()
                         .range(key, Range.unbounded());

        List<byte[]> updates = new ArrayList<>();

        if (records != null) {
            for (MapRecord<String, Object, Object> rec : records) {
                updates.add((byte[]) rec.getValue().get("update"));
            }
        }

        return updates;
    }

    @Override
    public void deleteUntil(String diagramId, String lastRecordIdStr) {
        String key = streamKey(diagramId);
        RecordId lastRecordId = RecordId.of(lastRecordIdStr);

        // Read all IDs up to lastRecordId
        List<MapRecord<String,Object,Object>> toDelete = 
            redisTemplate.opsForStream()
                         .range(key, Range.closed("0-0", lastRecordId.toString()));

        if (toDelete == null) return;

        List<RecordId> deleteIds = new ArrayList<>();
        for (MapRecord<String, Object, Object> record : toDelete) {
            deleteIds.add(record.getId());
        }

        if (!deleteIds.isEmpty()) {
            // XDEL key id1 id2 id3...
            redisTemplate.opsForStream().delete(key, deleteIds.toArray(new RecordId[0]));
        }
    }
}
