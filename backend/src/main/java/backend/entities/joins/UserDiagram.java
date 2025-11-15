package backend.entities.joins;

import backend.entities.Diagram;
import backend.entities.User;
import backend.user.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;

@Entity
public class UserDiagram {

    @EmbeddedId
    private UserDiagramId UUID = new UserDiagramId();

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @MapsId("diagramId")
    @JoinColumn(name = "diagram_id", nullable = false)
    private Diagram diagram;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}