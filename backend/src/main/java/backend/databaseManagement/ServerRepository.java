package backend.databaseManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.entities.Server;

@Repository
public interface ServerRepository extends JpaRepository<Server, Integer> {
}
