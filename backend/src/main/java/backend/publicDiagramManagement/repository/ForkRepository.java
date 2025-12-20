package backend.publicDiagramManagement.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import backend.entities.joins.PublicDiagramUserId;
import backend.entities.publicDiagramEntities.DiagramFork;
import backend.entities.publicDiagramEntities.PublicDiagram;

@Repository
public interface ForkRepository extends JpaRepository<DiagramFork, PublicDiagramUserId> {
    @Query("""
                SELECT pd
                FROM DiagramFork f
                JOIN PublicDiagram pd ON pd.id = f.originalDiagram.id
                WHERE f.id.userId = :userId
                ORDER BY f.forkedAt DESC
            """)
    Page<PublicDiagram> findForkedPublicDiagramsByUser(int userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM DiagramFork f WHERE f.id.publicDiagramId = :publicDiagramId")
    void deleteByOriginalDiagramId(UUID publicDiagramId);
}

