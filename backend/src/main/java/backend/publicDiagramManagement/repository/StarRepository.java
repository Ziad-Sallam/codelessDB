package backend.publicDiagramManagement.repository;

import backend.entities.joins.PublicDiagramUserId;
import backend.entities.publicDiagramEntities.DiagramStar;
import backend.entities.publicDiagramEntities.PublicDiagram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StarRepository extends JpaRepository<DiagramStar, PublicDiagramUserId> {

    @Query("""
        SELECT s.publicDiagram
        FROM DiagramStar s
        WHERE s.id.userId = :userId
        ORDER BY s.starredAt DESC
    """)
    Page<PublicDiagram> findStarredPublicDiagramsByUser(int userId, Pageable pageable);
}
