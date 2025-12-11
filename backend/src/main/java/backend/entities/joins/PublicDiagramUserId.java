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
public class PublicDiagramUserId implements Serializable {
    @Column(name = "user_id")
    private int userId;

    @Column(name = "public_diagram_id")
    private UUID publicDiagramId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublicDiagramUserId)) return false;
        PublicDiagramUserId that = (PublicDiagramUserId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(publicDiagramId, that.publicDiagramId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, publicDiagramId);
    }
}
