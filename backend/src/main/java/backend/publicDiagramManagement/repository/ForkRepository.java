package backend.publicDiagramManagement.repository;

import backend.entities.Diagram;
import backend.entities.joins.PublicDiagramUserId;
import backend.entities.publicDiagramEntities.DiagramFork;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ForkRepository extends JpaRepository<DiagramFork, PublicDiagramUserId> {
    @Query("""
        SELECT f.originalDiagram.diagram
        FROM DiagramFork f
        WHERE f.id.userId = :userId
        ORDER BY f.forkedAt DESC
    """)
    Page<Diagram> findForkedDiagramsByUser(int userId, Pageable pageable);
}

