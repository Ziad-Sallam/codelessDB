package backend.userDiagramManagement.repository;

import backend.entities.joins.UserDiagram;
import backend.entities.joins.UserDiagramId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.Optional;
import java.util.UUID;

public interface UserDiagramRepository extends JpaRepository<UserDiagram, UserDiagramId> {
    Optional<UserDiagram> findByUser_IdAndDiagram_Id(int userId, UUID diagramId);
    Page<UserDiagram> findAllByUser_IdAndDiagram_NameContainingIgnoreCaseAndDiagram_CreatedAtBetween(int user_id, String diagram_name, Date diagram_createdAt, Date diagram_createdAt2, Pageable pageable);
    boolean existsByUser_IdAndDiagram_Id(int id, UUID id1);
}
