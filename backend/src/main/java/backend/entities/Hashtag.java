package backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "hashtags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hashtag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true, length = 50)
  private String name;

  @ManyToMany(mappedBy = "hashtags")
  private Set<PublicDiagram> publicDiagrams = new HashSet<>();
}
