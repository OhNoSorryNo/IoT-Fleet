package IoTFleetManagement.repository;

import IoTFleetManagement.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for Role entities.
 * <p>
 * This interface provides methods to perform CRUD operations on Role entities.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its name.
     *
     * @param name the name of the role to find
     * @return the Role entity with the specified name, or null if not found
     */
    Role findRoleByName(String name);
}
