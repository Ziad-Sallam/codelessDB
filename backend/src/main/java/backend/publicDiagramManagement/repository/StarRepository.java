package backend.publicDiagramManagement.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import backend.entities.joins.PublicDiagramUserId;
import backend.entities.publicDiagramEntities.DiagramStar;
import backend.entities.publicDiagramEntities.PublicDiagram;

@Repository
public interface StarRepository extends JpaRepository<DiagramStar, PublicDiagramUserId> {

    @Query("""
                SELECT s.publicDiagram
                FROM DiagramStar s
                WHERE s.id.userId = :userId
                ORDER BY s.starredAt DESC
            """)
    Page<PublicDiagram> findStarredPublicDiagramsByUser(int userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM DiagramStar s WHERE s.id.publicDiagramId = :publicDiagramId")
    void deleteByIdPublicDiagramId(UUID publicDiagramId);
}
