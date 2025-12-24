package backend.collab.services;

import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateWriterTest {

	@Mock
	private ExecutorService executorService;

	@InjectMocks
	private UpdateWriter updateWriter;

	@Test
	void testSubmitWriteTask() {
		Runnable task = mock(Runnable.class);
		updateWriter.submitWriteTask(task);
		verify(executorService).submit(task);
	}
}
