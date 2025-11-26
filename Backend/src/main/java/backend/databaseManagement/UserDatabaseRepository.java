package backend.databaseManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import backend.entities.UserDatabase;

@Repository
public interface UserDatabaseRepository extends JpaRepository<UserDatabase, Integer> {
    
    
}
