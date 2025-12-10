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

    @Query(
            value = """
        SELECT pd.*
        FROM public_diagrams pd
        LEFT JOIN public_diagram_hashtags pht ON pd.diagram_id = pht.diagram_id
        LEFT JOIN hashtags h ON pht.hashtag_id = h.id
        WHERE (
            :name IS NULL
            OR :name = ''
            OR LOWER((SELECT d.name FROM diagrams d WHERE d.id = pd.diagram_id)) LIKE CONCAT('%', LOWER(:name), '%')
            OR LOWER(pd.short_description) LIKE CONCAT('%', LOWER(:name), '%')
            OR LOWER(pd.detailed_description) LIKE CONCAT('%', LOWER(:name), '%')
        )
        AND (
            :tags IS NULL
            OR :tags = ''
            OR h.name IN (:tags)
        )
        GROUP BY pd.diagram_id
        ORDER BY (pd.views + pd.forks * 2 + pd.stars * 3) DESC
      """,
            countQuery = """
        SELECT COUNT(DISTINCT pd.diagram_id)
        FROM public_diagrams pd
        LEFT JOIN public_diagram_hashtags pht ON pd.diagram_id = pht.diagram_id
        LEFT JOIN hashtags h ON pht.hashtag_id = h.id
        WHERE (
            :name IS NULL
            OR :name = ''
            OR LOWER((SELECT d.name FROM diagrams d WHERE d.id = pd.diagram_id)) LIKE CONCAT('%', LOWER(:name), '%')
            OR LOWER(pd.short_description) LIKE CONCAT('%', LOWER(:name), '%')
            OR LOWER(pd.detailed_description) LIKE CONCAT('%', LOWER(:name), '%')
        )
        AND (
            :tags IS NULL
            OR :tags = ''
            OR h.name IN (:tags)
        )
      """,
            nativeQuery = true
    )
    Page<PublicDiagram> searchPublicDiagrams(
            @Param("name") String name,
            @Param("tags") List<String> tags,
            Pageable pageable
    );
}
