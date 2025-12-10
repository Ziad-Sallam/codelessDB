package backend.publicDiagramManagement.repository;

import backend.entities.joins.UserDiagramId;
import backend.entities.publicDiagramEntities.DiagramView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ViewsRepository extends JpaRepository<DiagramView, UserDiagramId> {

    @Modifying
    @Query(
            value = """
           INSERT INTO diagram_views (user_id, diagram_id)
           VALUES (:userId, :diagramId)
           ON CONFLICT DO NOTHING
        """,
            nativeQuery = true
    )
    int insertIfNotExists(int userId, UUID diagramId);
}


