package backend.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import backend.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findById(int id);

    User findByEmail(String email);

    User findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query(value = """
            SELECT u FROM User u
            WHERE :search IS NULL
                OR :search = ''
                OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.bio) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<User> simpleSearchUsers(@Param("search") String search, Pageable pageable);

    @Query(value = """
            SELECT u FROM User u
            WHERE (
                :search IS NULL
                OR :search = ''
                OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.bio) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND NOT EXISTS (
                SELECT 1 FROM UserDatabaseAccess uda
                WHERE uda.user = u
                AND uda.database.id = :databaseId
            )
            """)
    Page<User> searchUsersExcludingDatabase(@Param("search") String search, @Param("databaseId") int databaseId,
            Pageable pageable);

    @Query(value = """
            SELECT
                u,
                COUNT(DISTINCT pd.id),
                u.totalStars,
                COALESCE(SUM(pd.views + pd.forks * 2 + pd.stars * 3), 0)
            FROM User u
            JOIN u.userDiagrams ud
            JOIN ud.diagram d
            JOIN d.publicDiagram pd
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
                OR EXISTS (
                    SELECT 1 FROM pd.hashtags h WHERE h.name IN (:tags)
                )
            )
            GROUP BY u.id
            ORDER BY COALESCE(SUM(pd.views + pd.forks * 2 + pd.stars * 3), 0) DESC
            """)
    Page<Object[]> searchUsersWithPublicStats(
            @Param("search") String search,
            @Param("tags") List<String> tags,
            Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM user_following WHERE follower_id = :followerId AND user_id = :userId", nativeQuery = true)
    long countFollowing(@Param("followerId") int followerId, @Param("userId") int userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_following (follower_id, user_id) VALUES (:followerId, :userId)", nativeQuery = true)
    void addFollowing(@Param("followerId") int followerId, @Param("userId") int userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_followers (user_id, follower_id) VALUES (:userId, :followerId)", nativeQuery = true)
    void addFollower(@Param("userId") int userId, @Param("followerId") int followerId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_following WHERE follower_id = :followerId AND user_id = :userId", nativeQuery = true)
    void removeFollowing(@Param("followerId") int followerId, @Param("userId") int userId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_followers WHERE user_id = :userId AND follower_id = :followerId", nativeQuery = true)
    void removeFollower(@Param("userId") int userId, @Param("followerId") int followerId);

    @Query(value = """
            SELECT
                u,
                (SELECT COUNT(pd.id) FROM PublicDiagram pd JOIN pd.diagram d JOIN d.userDiagrams ud WHERE ud.user = u AND ud.role = backend.user.Role.OWNER),
                u.totalStars,
                (SELECT COALESCE(SUM(pd.views + pd.forks * 2 + pd.stars * 3), 0) FROM PublicDiagram pd JOIN pd.diagram d JOIN d.userDiagrams ud WHERE ud.user = u AND ud.role = backend.user.Role.OWNER)
            FROM User target
            JOIN target.followers u
            WHERE target.id = :userId
            """)
    Page<Object[]> findFollowersWithStats(@Param("userId") int userId, Pageable pageable);

    @Query(value = """
            SELECT
                u,
                (SELECT COUNT(pd.id) FROM PublicDiagram pd JOIN pd.diagram d JOIN d.userDiagrams ud WHERE ud.user = u AND ud.role = backend.user.Role.OWNER),
                u.totalStars,
                (SELECT COALESCE(SUM(pd.views + pd.forks * 2 + pd.stars * 3), 0) FROM PublicDiagram pd JOIN pd.diagram d JOIN d.userDiagrams ud WHERE ud.user = u AND ud.role = backend.user.Role.OWNER)
            FROM User target
            JOIN target.following u
            WHERE target.id = :userId
            """)
    Page<Object[]> findFollowingWithStats(@Param("userId") int userId, Pageable pageable);
}
