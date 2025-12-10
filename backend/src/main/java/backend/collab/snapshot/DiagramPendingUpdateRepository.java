package backend.collab.snapshot;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import backend.entities.DiagramPendingUpdate;
import jakarta.transaction.Transactional;

@Repository
public interface DiagramPendingUpdateRepository extends JpaRepository<DiagramPendingUpdate, Long> {
	
	@Query("SELECT d.updateData FROM DiagramPendingUpdate d WHERE d.diagramId = :diagramId")
	List<byte[]> findAllUpdateDataByDiagramId(String diagramId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM DiagramPendingUpdate d WHERE d.diagramId = :diagramId")
	void deleteAllByDiagramId(String diagramId);

}
