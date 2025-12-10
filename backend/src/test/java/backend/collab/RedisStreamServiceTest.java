package backend.collab;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import backend.collab.services.RedisStreamService;

public class RedisStreamServiceTest {

	@Mock
	private RedisTemplate<String, byte[]> redisTemplate;

	@Mock
	private StreamOperations<String, Object, Object> streamOps;

	@InjectMocks
	private RedisStreamService redisStreamService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		when(redisTemplate.opsForStream()).thenReturn(streamOps);
	}

	/*
	 * -----------------------------------------------------------------------
	 * addUpdate()
	 * ---------------------------------------------------------------------
	 */

	@Test
	void testAddUpdate_callsXadd() {
		String diagramId = "123";
		byte[] update = "hello".getBytes();

		redisStreamService.addUpdate(diagramId, update);

		verify(streamOps, times(1)).add(any());
	}

	/*
	 * -----------------------------------------------------------------------
	 * getAllUpdates()
	 * ---------------------------------------------------------------------
	 */

	@Test
	void testGetAllUpdates_diagramNotExists() {
		when(redisTemplate.hasKey("stream:123")).thenReturn(false);

		List<byte[]> result = redisStreamService.getAllUpdates("123");

		assertTrue(result.isEmpty());
		verify(streamOps, never()).range(anyString(), any());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testGetAllUpdates_success() {
		String key = "stream:ABC";

		when(redisTemplate.hasKey(key)).thenReturn(true);

		byte[] u1 = "A".getBytes();
		byte[] u2 = "B".getBytes();

		MapRecord<String, Object, Object> r1 = MapRecord.create(key, Collections.singletonMap("update", u1));
		MapRecord<String, Object, Object> r2 = MapRecord.create(key, Collections.singletonMap("update", u2));

		when(streamOps.range(eq(key), any(Range.class)))
				.thenReturn(Arrays.asList(r1, r2));

		List<byte[]> result = redisStreamService.getAllUpdates("ABC");

		assertEquals(2, result.size());
		assertArrayEquals(u1, result.get(0));
		assertArrayEquals(u2, result.get(1));
	}

	/*
	 * -----------------------------------------------------------------------
	 * deleteUntil()
	 * ---------------------------------------------------------------------
	 */

	@Test
	void testDeleteUntil_diagramDoesNotExist() {
		when(redisTemplate.hasKey("stream:55")).thenReturn(false);

		redisStreamService.deleteUntil("55", "0-1");

		verify(streamOps, never()).range(anyString(), any());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testDeleteUntil_success() {
		String key = "stream:999";

		when(redisTemplate.hasKey(key)).thenReturn(true);

		RecordId id1 = RecordId.of("0-1");
		RecordId id2 = RecordId.of("0-2");

		MapRecord<String, Object, Object> r1 = MapRecord.create(key, Collections.emptyMap()).withId(id1);
		MapRecord<String, Object, Object> r2 = MapRecord.create(key, Collections.emptyMap()).withId(id2);

		when(streamOps.range(eq(key), any(Range.class)))
				.thenReturn(Arrays.asList(r1, r2));

		redisStreamService.deleteUntil("999", "0-2");

		verify(streamOps, times(1)).delete(eq(key), any(RecordId[].class));
	}

	/*
	 * -----------------------------------------------------------------------
	 * diagramExists()
	 * ---------------------------------------------------------------------
	 */

	@Test
	void testDiagramExists_true() {
		when(redisTemplate.hasKey("stream:1")).thenReturn(true);

		assertTrue(redisStreamService.diagramExists("1"));
	}

	@Test
	void testDiagramExists_false() {
		when(redisTemplate.hasKey("stream:1")).thenReturn(false);

		assertFalse(redisStreamService.diagramExists("1"));
	}

	/*
	 * -----------------------------------------------------------------------
	 * removeDiagramHistory()
	 * ---------------------------------------------------------------------
	 */

	@Test
	void testRemoveDiagramHistory() {
		redisStreamService.removeDiagramHistory("7");

		verify(redisTemplate, times(1)).delete("stream:7");
	}
}
