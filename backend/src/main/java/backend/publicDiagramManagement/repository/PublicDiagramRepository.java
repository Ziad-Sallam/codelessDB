package backend.publicDiagramManagement.repository;

import backend.entities.publicDiagramEntities.PublicDiagram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PublicDiagramRepository extends JpaRepository<PublicDiagram, UUID> {

    @Modifying
    @Query("""
        UPDATE PublicDiagram p
        SET p.views = p.views + 1
        WHERE p.id = :id
    """)
    void incrementViews(@Param("id") UUID id);

    @Modifying
    @Query("""
        UPDATE PublicDiagram p
        SET p.forks = p.forks + 1
        WHERE p.id = :id
        """)
    void incrementForks(@Param("id") UUID id);
}
