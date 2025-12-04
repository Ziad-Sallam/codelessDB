package backend.entities.publicDiagramEntities;

import java.sql.Date;
import java.util.*;

import backend.entities.Diagram;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "public_diagrams")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicDiagram {

    @Id
    @GeneratedValue
    @Column(name = "diagram_id")
    private UUID id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "diagram_id")
    private Diagram diagram;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Size(min = 5, max = 500)
    private String shortDescription;

    @Column(nullable = true, columnDefinition = "TEXT")
    @Size(max = 2000)
    private String detailedDescription;

    @Column(nullable = false)
    private int stars = 0;

    @Column(nullable = false)
    private int forks = 0;

    @Column(nullable = false)
    private int views = 0;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date publishedAt;

    @Column
    @UpdateTimestamp
    private Date lastModified;

    @Lob
    @Column(nullable = false)
    private String ddl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "public_diagram_hashtags",
            joinColumns = @JoinColumn(name = "diagram_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<Hashtag> hashtags = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "diagram", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CannedQueriesDiagrams> queries = new ArrayList<>();
}
