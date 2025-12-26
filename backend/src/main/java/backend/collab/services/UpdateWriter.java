package backend.collab.services;

import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class UpdateWriter {

	@Autowired
	@Qualifier("collabWriter")
	private ExecutorService writeExecutor;

	public void submitWriteTask(Runnable task) {
		writeExecutor.submit(task);
	}
}
