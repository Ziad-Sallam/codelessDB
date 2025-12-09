package backend.databaseManagement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.entities.UserDatabase;

@Repository
public interface UserDatabaseRepository extends JpaRepository<UserDatabase, Integer> {
List<UserDatabase> findByOwnerId(Integer ownerId);
boolean existsByNameAndOwnerId(String name, Integer ownerId);
}
