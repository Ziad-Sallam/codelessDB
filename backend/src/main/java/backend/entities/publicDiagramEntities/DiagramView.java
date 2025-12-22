package backend.entities.publicDiagramEntities;

import java.time.LocalDateTime;

import backend.entities.joins.PublicDiagramUserId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "diagram_views",
        uniqueConstraints = @UniqueConstraint(columnNames = {"public_diagram_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DiagramView {

    @EmbeddedId
    private PublicDiagramUserId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("publicDiagramId")
    @JoinColumn(name = "public_diagram_id", nullable = false)
    private PublicDiagram publicDiagram;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime firstViewedAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime lastViewedAt;
}
