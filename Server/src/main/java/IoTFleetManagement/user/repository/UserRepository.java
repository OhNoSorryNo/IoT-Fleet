package IoTFleetManagement.user.repository;

import IoTFleetManagement.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for User entities.
 * <p>
 * This interface provides methods to perform CRUD operations on User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by its username.
     *
     * @param username the username of the user to find
     * @return an Optional containing the User entity if found, or empty if not found
     */
    Optional<User> findByUsername(String username);
    /**
     * Find a user by their email.
     *
     * @param email the email of the user to find
     * @return an Optional containing the User entity if found, or empty if not found
     */
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
}
