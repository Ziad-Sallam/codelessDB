package backend.entities;

import java.sql.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "canned_queries_diagrams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CannedQueriesDiagrams {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String query;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  private Date createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "diagram_id", nullable = false)
  private Diagram diagram;
}
