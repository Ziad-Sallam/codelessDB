package backend.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import backend.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
	User findById(int id);

	User findByEmail(String email);

	User findByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);
    @Query(
            value = """
            SELECT 
                u.*,
                COUNT(DISTINCT pd.diagram_id) AS publicCount,
                COALESCE(SUM(pd.stars), 0) AS totalStars,
                COALESCE(SUM(pd.views + pd.forks * 2 + pd.stars * 3), 0) AS score
            FROM users u
            JOIN user_diagram ud 
                ON ud.user_id = u.id
            JOIN diagrams d 
                ON d.id = ud.diagram_id
            JOIN public_diagrams pd 
                ON pd.diagram_id = d.id
            LEFT JOIN public_diagram_hashtags pht 
                ON pht.diagram_id = pd.diagram_id
            LEFT JOIN hashtags h 
                ON h.id = pht.hashtag_id

            WHERE (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(u.username) LIKE CONCAT('%', LOWER(:search), '%')
                    OR LOWER(u.bio) LIKE CONCAT('%', LOWER(:search), '%')
                    OR LOWER(d.name) LIKE CONCAT('%', LOWER(:search), '%')
                )
              AND (
                    :tags IS NULL
                    OR CARDINALITY(:tags) = 0
                    OR h.name IN (:tags)
                )

            GROUP BY u.id

            ORDER BY score DESC
            """,

            countQuery = """
            SELECT COUNT(DISTINCT u.id)
            FROM users u
            JOIN user_diagram ud 
                ON ud.user_id = u.id
            JOIN diagrams d 
                ON d.id = ud.diagram_id
            JOIN public_diagrams pd 
                ON pd.diagram_id = d.id
            LEFT JOIN public_diagram_hashtags pht 
                ON pht.diagram_id = pd.diagram_id
            LEFT JOIN hashtags h 
                ON h.id = pht.hashtag_id

            WHERE (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(u.username) LIKE CONCAT('%', LOWER(:search), '%')
                    OR LOWER(u.bio) LIKE CONCAT('%', LOWER(:search), '%')
                    OR LOWER(d.name) LIKE CONCAT('%', LOWER(:search), '%')
                )
              AND (
                    :tags IS NULL
                    OR CARDINALITY(:tags) = 0
                    OR h.name IN (:tags)
                )
            """,

            nativeQuery = true
    )
    Page<Object[]> searchUsersWithPublicStats(
            @Param("search") String search,
            @Param("tags") List<String> tags,
            Pageable pageable
    );
}