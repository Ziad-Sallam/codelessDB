package backend.userDiagramManagement.repository;

import backend.entities.Diagram;
import backend.entities.User;
import backend.entities.joins.UserDiagram;
import backend.entities.joins.UserDiagramId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDiagramRepository extends JpaRepository<UserDiagram, UserDiagramId> {

    // 1. Find a single UserDiagram by user and diagram IDs
    Optional<UserDiagram> findByUser_IdAndDiagram_Id(int userId, UUID diagramId);

    // 2. Search diagrams for a user with name containing and createdAt between
    // dates, with pagination
    Page<UserDiagram> findAllByUser_IdAndDiagram_NameContainingIgnoreCaseAndDiagram_CreatedAtBetween(
            int userId,
            String diagramName,
            LocalDateTime createdAtStart,
            LocalDateTime createdAtEnd,
            Pageable pageable);

    // 3. Check existence of UserDiagram for a specific user and diagram
    boolean existsByUser_IdAndDiagram_Id(int userId, UUID diagramId);

    // 4. Fetch all diagrams for a user with pagination
    Page<UserDiagram> findByUser_Id(Integer userId, Pageable pageable);

    // 5. Fetch all contributors for a diagram
    List<UserDiagram> findByDiagram_Id(UUID diagramId);

    boolean existsByDiagram_Id(UUID diagramId);

    UserDiagram findFirstByDiagram_Id(UUID diagramId);

    @Query("""
                SELECT ud.diagram
                FROM UserDiagram ud
                WHERE ud.user.id = :userId
                  AND ud.role = 'OWNER'
                  AND NOT EXISTS (
                    SELECT 1 FROM PublicDiagram pd WHERE pd.diagram.id = ud.diagram.id
                  )
            """)
    Page<Diagram> findUnpublishedDiagramsByUser(int userId, Pageable pageable);
}
