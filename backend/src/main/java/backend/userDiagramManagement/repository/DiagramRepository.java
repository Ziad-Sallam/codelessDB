package backend.userDiagramManagement.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import backend.entities.Diagram;

public interface DiagramRepository extends JpaRepository<Diagram, UUID> {
    @Query("""
            SELECT COUNT(pd)
            FROM PublicDiagram pd
            JOIN pd.diagram d
            JOIN d.userDiagrams ud
            WHERE ud.user.id = :userId
              AND ud.role = backend.user.Role.OWNER
            """)
    Long countPublicDiagramsByOwner(@Param("userId") int userId);

    @Query("""
            SELECT COALESCE(SUM(pd.stars), 0)
            FROM PublicDiagram pd
            JOIN pd.diagram d
            JOIN d.userDiagrams ud
            WHERE ud.user.id = :userId
              AND ud.role = backend.user.Role.OWNER
            """)
    Long sumStarsOfPublicDiagramsByOwner(@Param("userId") int userId);
}
