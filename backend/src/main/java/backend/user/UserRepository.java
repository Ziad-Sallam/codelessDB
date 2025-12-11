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

    @Query(value = """
            SELECT
                u,
                COUNT(DISTINCT pd.id),
                COALESCE(SUM(pd.stars), 0),
                COALESCE(SUM(pd.views + pd.forks * 2 + pd.stars * 3), 0)
            FROM User u
            JOIN u.userDiagrams ud
            JOIN ud.diagram d
            JOIN d.publicDiagram pd
            LEFT JOIN pd.hashtags h
            WHERE ud.role = 'OWNER'
            AND (
                :search IS NULL
                OR :search = ''
                OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.bio) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (
                (:tags) IS NULL
                OR h.name IN (:tags)
            )
            GROUP BY u.id
            ORDER BY COALESCE(SUM(pd.views + pd.forks * 2 + pd.stars * 3), 0) DESC
            """)
    Page<Object[]> searchUsersWithPublicStats(
            @Param("search") String search,
            @Param("tags") List<String> tags,
            Pageable pageable);
}