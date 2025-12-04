package backend.entities.publicDiagramEntities;

import backend.entities.joins.UserDiagramId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Date;

@Entity
@Table(name = "diagram_stars",
        uniqueConstraints = @UniqueConstraint(columnNames = {"diagram_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagramStar {

    @EmbeddedId
    private UserDiagramId id;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date starredAt;
}
