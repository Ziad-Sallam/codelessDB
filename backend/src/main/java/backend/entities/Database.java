package backend.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "databases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Database {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 100)
  private String name;

  @OneToMany(mappedBy = "database", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CannedQueriesDB> queries = new ArrayList<>();

  // @Column(nullable = false)
  // private String password;
}
