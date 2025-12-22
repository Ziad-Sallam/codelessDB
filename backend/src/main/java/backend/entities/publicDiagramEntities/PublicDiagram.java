package backend.entities.publicDiagramEntities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import backend.entities.Diagram;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "public_diagrams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"diagram", "hashtags", "cannedQueries", "stars", "views", "forks"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PublicDiagram {

    @Id
    @Column(name = "diagram_id")
    @EqualsAndHashCode.Include
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "diagram_id")
    private Diagram diagram;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Size(min = 1, max = 500)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000)
    private String detailedDescription;

    @Builder.Default
    private int stars = 0;
    @Builder.Default
    private int forks = 0;
    @Builder.Default
    private int views = 0;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime publishedAt;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "public_diagram_hashtags",
            joinColumns = @JoinColumn(name = "diagram_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<Hashtag> hashtags = new HashSet<>();

    /* Canned queries attached to the public diagram */
    @Builder.Default
    @OneToMany(mappedBy = "publicDiagram", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CannedQueriesDiagrams> cannedQueries = new HashSet<>();

    /* Stars, views, forks - reverse side (optional, lazy loaded) */
    @Builder.Default
    @OneToMany(mappedBy = "publicDiagram", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DiagramStar> starsEntities = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "publicDiagram", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DiagramView> viewEntities = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "originalDiagram", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DiagramFork> forkEntities = new HashSet<>();
}
