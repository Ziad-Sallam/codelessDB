package backend.entities;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import backend.entities.joins.UserDiagram;
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

  @Column(nullable = false, length = 200)
  private String name;

  @Column(columnDefinition = "JSON")
  private String content; // to be continued

  private String thumbnail;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  private Date createdAt;

  @Column(nullable = false)
  @UpdateTimestamp
  private Date lastModified;

  @OneToMany(mappedBy = "diagram", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CannedQueriesDiagrams> queries = new ArrayList<>();

  @OneToOne(mappedBy = "diagram", cascade = CascadeType.ALL, orphanRemoval = true)
  private PublicDiagram publicDiagram;

  @OneToMany(mappedBy = "diagram", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<UserDiagram> userDiagrams = new HashSet<>();

}
