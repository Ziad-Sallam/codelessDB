package backend.entities;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "public_diagrams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicDiagram {

    @OneToOne(optional = false)
    @EmbeddedId
    @JoinColumn(name = "diagram_id", nullable = false, unique = true)
    private Diagram diagram;

    @Column(nullable = false)
    private int likes = 0;

    @Column(nullable = false)
    private int dislikes = 0;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date publishedAt;

    @ManyToMany
    @JoinTable(name = "public_diagram_hashtags", joinColumns = @JoinColumn(name = "public_diagram_id"), inverseJoinColumns = @JoinColumn(name = "hashtag_id"))
    private List<Hashtag> hashtags = new ArrayList<>();
}
