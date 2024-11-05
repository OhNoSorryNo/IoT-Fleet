package IoTFleetManagement.repository;

import IoTFleetManagement.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean findByName(String name);
}
