package backend.publicDiagramManagement.repository;

import backend.entities.publicDiagramEntities.PublicDiagram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Modifying
    @Query("""
                UPDATE PublicDiagram p
                SET p.stars = p.stars + 1
                WHERE p.id = :id
            """)
    void incrementStar(@Param("id") UUID id);

    @Modifying
    @Query("""
                UPDATE PublicDiagram p
                SET p.stars = p.stars - 1
                WHERE p.id = :id AND p.stars > 0
            """)
    void decrementStar(@Param("id") UUID id);

    @Query(value = """
              SELECT pd
              FROM PublicDiagram pd
              LEFT JOIN pd.hashtags h
              LEFT JOIN pd.diagram d
              WHERE (
                  :name IS NULL
                  OR :name = ''
                  OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))
                  OR LOWER(pd.shortDescription) LIKE LOWER(CONCAT('%', :name, '%'))
                  OR LOWER(pd.detailedDescription) LIKE LOWER(CONCAT('%', :name, '%'))
              )
              AND (
                  (:tags) IS NULL
                  OR h.name IN (:tags)
              )
              GROUP BY pd.id
              ORDER BY (pd.views + pd.forks * 2 + pd.stars * 3) DESC
            """)
    Page<PublicDiagram> searchPublicDiagrams(
            @Param("name") String name,
            @Param("tags") List<String> tags,
            Pageable pageable
    );

    @Query("""
        SELECT pd
        FROM PublicDiagram pd
        JOIN pd.diagram d
        JOIN d.userDiagrams ud
        JOIN ud.user u
        WHERE u.username = :username
          AND ud.role = backend.user.Role.OWNER
          AND d.publicDiagram IS NOT NULL
    """)
    Page<PublicDiagram> findPublicDiagramsByOwner(
            @Param("username") String username,
            Pageable pageable
    );


    @Query("""
        SELECT pd
        FROM PublicDiagram pd
        JOIN pd.starsEntities se
        JOIN User u ON u.id = se.id.userId
        WHERE u.username = :username
        ORDER BY se.starredAt DESC
    """)
    Page<PublicDiagram> findStaredPublicDiagramsByUser(
            @Param("username") String username,
            Pageable pageable
    );
}
