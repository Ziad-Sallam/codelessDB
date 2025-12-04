package backend.entities.publicDiagramEntities;

import backend.entities.joins.UserDiagramId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Date;

@Entity
@Table(name = "diagram_views")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagramView {

    @EmbeddedId
    private UserDiagramId id;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date firstViewedAt;

    @Column
    private Date lastViewedAt;
}
