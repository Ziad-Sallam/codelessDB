package backend.entities.joins;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDiagramId implements Serializable {
    @Column(name = "user_id")
    private int userId;

    @Column(name = "diagram_id")
    private UUID diagramId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDiagramId)) return false;
        UserDiagramId that = (UserDiagramId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(diagramId, that.diagramId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, diagramId);
    }
}