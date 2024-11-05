package IoTFleetManagement.config;

import org.springframework.boot.CommandLineRunner;
import IoTFleetManagement.model.Role;
import IoTFleetManagement.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class initializes the database table called {@code roles} which stores the roles when the application starts.
 * It ensures that required roles such as ADMIN and USER are always present.
 * <p>
 * Implements {@link CommandLineRunner} to execute database initialization logic upon application startup.
 */
@Component
public class RoleDBInitializer implements CommandLineRunner {
    // Logger to log information and errors
    private static final Logger logger = LoggerFactory.getLogger(RoleDBInitializer.class);

    // Role constants to avoid hardcoding strings multiple times
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    // RoleRepository is used to interact with the database for Role entities
    private final RoleRepository roleRepository;

    /**
     * Constructor to inject RoleRepository dependency.
     *
     * @param roleRepository the repository to manage role data in the database
     */
    @Autowired
    public RoleDBInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * This method runs when the application starts.
     * It checks if the roles "ROLE_ADMIN" and "ROLE_USER" exist in the database, and if not, it inserts them.
     *
     * @param args command line arguments passed at startup
     */
    @Override
    public void run(String... args) {
        try {
            logger.debug("Starting role initialization process...");
            // Check if ROLE_ADMIN exists; if not, add it to the database
            if (!roleRepository.findByName(ROLE_ADMIN)) {
                roleRepository.save(new Role(ROLE_ADMIN));
                logger.info("Inserted ROLE_ADMIN");
            } else {
                logger.debug("ROLE_ADMIN already exists in the database.");
            }
            // Check if ROLE_USER exists; if not, add it to the database
            if (!roleRepository.findByName(ROLE_USER)) {
                roleRepository.save(new Role(ROLE_USER));
                logger.info("Inserted ROLE_USER");
            } else {
                logger.debug("ROLE_USER already exists in the database.");
            }
            logger.debug("Role initialization process completed.");
        } catch (Exception e) {
            // Log any exceptions that occur during role initialization
            logger.error("Error initializing roles in the database", e);
        }
    }
}
