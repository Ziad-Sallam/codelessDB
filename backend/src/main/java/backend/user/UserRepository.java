package backend.user;

import backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
	User findById(int id);

	User findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);
}