package backend.publicDiagramManagement.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import backend.entities.joins.PublicDiagramUserId;
import backend.entities.publicDiagramEntities.DiagramView;

@Repository
public interface ViewsRepository extends JpaRepository<DiagramView, PublicDiagramUserId> {

    @Modifying
    @Query(value = """
               INSERT IGNORE INTO diagram_views (user_id, public_diagram_id)
               VALUES (:userId, :diagramId)
            """, nativeQuery = true)
    int insertIfNotExists(int userId, UUID diagramId);

    void deleteByIdPublicDiagramId(UUID publicDiagramId);
}
