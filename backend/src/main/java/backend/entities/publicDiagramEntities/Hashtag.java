package backend.entities.publicDiagramEntities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hashtags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "publicDiagrams")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Hashtag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Builder.Default
    @ManyToMany(mappedBy = "hashtags")
    private Set<PublicDiagram> publicDiagrams = new HashSet<>();
}
