package backend.collab.snapshot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import backend.collab.services.UpdateWriter;
import backend.entities.Diagram;
import backend.entities.joins.UserDiagram;
import backend.user.Role;
import backend.userDiagramManagement.repository.DiagramRepository;
import backend.userDiagramManagement.service.UserDiagramService;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

	@Mock
	private DiagramRepository diagramRepository;

	@Mock
	private UserDiagramService userDiagramService;

	@Mock
	private YjsSnapshotClient snapshotClient;

	@Mock
	private UpdateWriter updateWriter;

	@InjectMocks
	private SnapshotService snapshotService;

	private UUID diagramId = UUID.randomUUID();
	private int userId = 1;

	@Test
	void testGetDiagramMetadata_Success() {
		UserDiagram ud = mock(UserDiagram.class);
		when(ud.getRole()).thenReturn(Role.WRITER);

		Diagram diagram = new Diagram();
		diagram.setName("Test Diagram");

		when(userDiagramService.getUserDiagramOrThrow(userId, diagramId)).thenReturn(ud);
		when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);

		SnapshotDto result = snapshotService.getDiagramMetadata(userId, diagramId);

		assertEquals("Test Diagram", result.getDiagramName());
		assertEquals(Role.WRITER, result.getRole());
	}

	@Test
	void testGetDiagramSnapshot_Success() {
		Diagram diagram = new Diagram();
		byte[] content = new byte[] { 1, 2, 3 };
		diagram.setContent(content);

		when(userDiagramService.getUserDiagramOrThrow(userId, diagramId)).thenReturn(mock(UserDiagram.class));
		when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);

		byte[] result = snapshotService.getDiagramSnapshot(userId, diagramId);

		assertArrayEquals(content, result);
	}

	@Test
	void testTakeSnapshot_SubmitsTask() {
		String dId = diagramId.toString();

		snapshotService.takeSnapshot(dId);

		verify(updateWriter).submitWriteTask(any(Runnable.class));
	}

	@Test
	void testTakeSnapshotThread_Success() {
		String dId = diagramId.toString();
		Diagram diagram = new Diagram();
		byte[] oldContent = new byte[] { 10 };
		diagram.setContent(oldContent);

		when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);

		byte[] newSnapshot = new byte[] { 99, 88 };
		when(snapshotClient.snapshot(eq(dId), eq(oldContent))).thenReturn(newSnapshot);

		snapshotService.takeSnapshotThread(dId);

		verify(diagramRepository).save(diagram);
		assertArrayEquals(newSnapshot, diagram.getContent());
	}

	@Test
	void testTakeSnapshotThread_EmptyResponse_DoesNotSave() {
		String dId = diagramId.toString();
		Diagram diagram = new Diagram();
		diagram.setContent(new byte[] { 10 });

		when(userDiagramService.getDiagramOrThrow(diagramId)).thenReturn(diagram);

		when(snapshotClient.snapshot(any(), any())).thenReturn(null);

		snapshotService.takeSnapshotThread(dId);

		verify(diagramRepository, never()).save(diagram);
	}
}
