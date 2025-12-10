package backend.collab.snapshot;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SnapshotDto {
	private byte[] snapshot;
	
	private List<byte[]> updates;
}
