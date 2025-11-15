package backend.entities.joins;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public class UserDiagramId implements Serializable {
	private int userId;
	private UUID diagramId;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (!(o instanceof UserDiagramId))
			return false;

		UserDiagramId that = (UserDiagramId) o;
		return Objects.equals(userId, that.userId) &&
				Objects.equals(diagramId, that.diagramId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, diagramId);
	}
}