package backend.publicDiagramManagement.repository;

import backend.entities.publicDiagramEntities.PublicDiagram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PublicDiagramRepository extends JpaRepository<PublicDiagram, UUID> {
}
