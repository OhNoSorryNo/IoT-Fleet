package IoTFleetManagement.config;

import org.springframework.boot.CommandLineRunner;
import IoTFleetManagement.model.Role;
import IoTFleetManagement.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleDBInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleDBInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if roles already exist; if not, add them
        if (roleRepository.findByName("ROLE_ADMIN") == null) {
            roleRepository.save(new Role("ROLE_ADMIN"));
            System.out.println("Inserted ROLE_ADMIN");
        }
        if (roleRepository.findByName("ROLE_USER") == null) {
            roleRepository.save(new Role("ROLE_USER"));
            System.out.println("Inserted ROLE_USER");
        }
    }

}
