package backend.collab.snapshot;

import java.util.List;

import backend.user.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SnapshotDto {
	private byte[] snapshot;

	private String diagramName;	
	
	private Role role;
}
