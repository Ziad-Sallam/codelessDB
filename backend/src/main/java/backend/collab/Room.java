package backend.collab;

import java.util.UUID;

import lombok.Data;
import lombok.Getter;

interface IRoom {
	void close();
	void addCollaborator(Collaborator collaborator);
	void removeCollaborator(String username);
}

@Getter
public class Room implements IRoom {
	private static int MAX_COLLABORATORS = 15;
	
	private UUID diagramId;
	
	private Collaborator[] collaborators;

	private int numOfCollaborators;
	
	private String lastSnapshot;

	public Room(UUID diagramId) {
		this.collaborators = new Collaborator[MAX_COLLABORATORS];
		this.diagramId = diagramId;
		this.lastSnapshot = "";
		this.numOfCollaborators = 0;
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException("Unimplemented method 'close'");
	}

	@Override
	public void addCollaborator(Collaborator collaborator) {
		collaborators[numOfCollaborators++] = collaborator;
	}

	@Override
	public void removeCollaborator(String username) {
		throw new UnsupportedOperationException("Unimplemented method 'removeCollaborator'");
	}
}
