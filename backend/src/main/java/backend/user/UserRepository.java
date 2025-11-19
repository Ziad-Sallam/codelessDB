package backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import backend.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	User findById(int id);

	User findByEmail(String email);

	User findByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);
}