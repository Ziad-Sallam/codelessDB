package backend.entities.publicDiagramEntities;

import backend.entities.joins.UserDiagramId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Date;

@Entity
@Table(name = "diagram_forks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagramFork {

    @EmbeddedId
    private UserDiagramId id; // user who forked + original diagram

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("diagramId")
    @JoinColumn(name = "diagram_id")
    private PublicDiagram originalDiagram;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date forkedAt;
}
