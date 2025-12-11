package backend.publicDiagramManagement.repository;

import backend.entities.publicDiagramEntities.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Integer> {
    Optional<Hashtag> findByName(String name);
}
