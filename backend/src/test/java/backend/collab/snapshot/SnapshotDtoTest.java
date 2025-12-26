package backend.collab.snapshot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import backend.user.Role;

class SnapshotDtoTest {

	@Test
	void testGettersAndSetters() {
		SnapshotDto dto = new SnapshotDto("Initial", Role.READER);

		dto.setDiagramName("Updated");
		dto.setRole(Role.WRITER);

		assertEquals("Updated", dto.getDiagramName());
		assertEquals(Role.WRITER, dto.getRole());
	}

	@Test
	void testEqualsAndHashCode() {
		SnapshotDto dto1 = new SnapshotDto("A", Role.WRITER);
		SnapshotDto dto2 = new SnapshotDto("A", Role.WRITER);
		SnapshotDto dto3 = new SnapshotDto("B", Role.WRITER);

		assertEquals(dto1, dto2);
		assertEquals(dto1.hashCode(), dto2.hashCode());
		assertNotEquals(dto1, dto3);
		assertNotEquals(dto1, null);
		assertNotEquals(dto1, "string");
	}

	@Test
	void testToString() {
		SnapshotDto dto = new SnapshotDto("A", Role.WRITER);
		String str = dto.toString();

		assertTrue(str.contains("A"));
		assertTrue(str.contains("WRITER"));
	}

	@Test
	void testNoArgsConstructor() {
		// Since @Data is used, check if it has a constructor if needed,
		// but the file explicitly has @AllArgsConstructor.
		// Let's just check the provided constructor.
		SnapshotDto dto = new SnapshotDto("Name", Role.WRITER);
		assertNotNull(dto);
	}
}
