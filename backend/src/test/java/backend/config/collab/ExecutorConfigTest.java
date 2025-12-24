package backend.config.collab;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;

class ExecutorConfigTest {

	@Test
	void testCollabWriterBean() {
		ExecutorConfig config = new ExecutorConfig();
		ExecutorService executor = config.collabWriter();

		assertNotNull(executor);
		assertTrue(executor instanceof ThreadPoolExecutor);

		ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
		assertEquals(2, tpe.getCorePoolSize());
		assertEquals(8, tpe.getMaximumPoolSize());
	}
}
