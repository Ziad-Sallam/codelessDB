package backend.entities;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import backend.entities.publicDiagramEntities.CannedQueriesDiagrams;
import backend.entities.publicDiagramEntities.PublicDiagram;
import jakarta.persistence.*;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import backend.entities.joins.UserDiagram;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diagrams")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diagram {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Builder.Default
  @Column(nullable = false, length = 200)
  private String name = "Untitled Diagram";
  
  @Column(columnDefinition = "JSON")
  private String content;

  @Lob
  @Column(nullable = false)
  private String ddl;
  
  private String thumbnail;
  
  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  private Date createdAt;
  
  @Column(nullable = false)
  @UpdateTimestamp
  private Date lastModified;

  @OneToOne(mappedBy = "diagram", cascade = CascadeType.ALL, orphanRemoval = true)
  private PublicDiagram publicDiagram;
  
  @Builder.Default
  @OneToMany(mappedBy = "diagram", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<UserDiagram> userDiagrams = new HashSet<>();
}
