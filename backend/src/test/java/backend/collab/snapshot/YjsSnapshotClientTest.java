package backend.collab.snapshot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class YjsSnapshotClientTest {

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private YjsSnapshotClient client;

	@BeforeEach
	void setUp() throws Exception {
		// Set @Value field via reflection
		Field field = YjsSnapshotClient.class.getDeclaredField("snapShotUrl");
		field.setAccessible(true);
		field.set(client, "http://localhost:3000");
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSnapshot() {
		String diagramId = "d-1";
		byte[] existing = new byte[] { 1 };
		byte[] expectedResponse = new byte[] { 2 };

		ResponseEntity<byte[]> responseEntity = mock(ResponseEntity.class);
		when(responseEntity.getBody()).thenReturn(expectedResponse);

		when(restTemplate.exchange(
				eq("http://localhost:3000/snapshot/" + diagramId),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				eq(byte[].class))).thenReturn(responseEntity);

		byte[] result = client.snapshot(diagramId, existing);

		assertArrayEquals(expectedResponse, result);
	}

	@Test
	void testSnapshot_NullExisting() {
		String diagramId = "d-1";
		// existing is null
		byte[] expectedResponse = new byte[] { 2 };

		@SuppressWarnings("unchecked")
		ResponseEntity<byte[]> responseEntity = mock(ResponseEntity.class);
		when(responseEntity.getBody()).thenReturn(expectedResponse);

		when(restTemplate.exchange(
				eq("http://localhost:3000/snapshot/" + diagramId),
				eq(HttpMethod.POST),
				any(HttpEntity.class),
				eq(byte[].class))).thenReturn(responseEntity);

		byte[] result = client.snapshot(diagramId, null);

		assertArrayEquals(expectedResponse, result);
	}
}
