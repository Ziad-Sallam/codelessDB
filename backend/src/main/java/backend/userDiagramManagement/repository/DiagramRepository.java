package backend.userDiagramManagement.repository;

import backend.entities.Diagram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DiagramRepository extends JpaRepository<Diagram, UUID> {
}
