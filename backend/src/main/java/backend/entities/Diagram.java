package backend.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import backend.entities.joins.UserDiagram;
import backend.entities.publicDiagramEntities.PublicDiagram;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "diagrams")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Diagram {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Builder.Default
    @Column(nullable = false, length = 200)
    private String name = "Untitled Diagram";

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] content;

    @Builder.Default
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String ddl = "";

    private String thumbnail;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime lastModified;

    @OneToOne(mappedBy = "diagram", cascade = CascadeType.ALL, orphanRemoval = true)
    private PublicDiagram publicDiagram;

    @Builder.Default
    @OneToMany(mappedBy = "diagram", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserDiagram> userDiagrams = new HashSet<>();
}
