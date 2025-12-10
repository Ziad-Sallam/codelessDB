package backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "updates", indexes = {
		@Index(name = "idx_diagram_updates_diagram", columnList = "diagramId")
		// @Index(name = "idx_diagram_updates_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagramPendingUpdate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String diagramId;

	@Lob
	@Column(nullable = false)
	private byte[] updateData;

	// @Column(nullable = false, updatable = false)
	// private long createdAt;

	// @PrePersist
	// public void prePersist() {
	// 	createdAt = System.currentTimeMillis();
	// }
}
