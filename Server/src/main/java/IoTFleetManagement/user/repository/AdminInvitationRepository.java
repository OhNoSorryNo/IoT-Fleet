package IoTFleetManagement.user.repository;

import IoTFleetManagement.user.model.AdminInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link AdminInvitation} entities.
 * <p>
 * This interface extends {@link JpaRepository} to provide basic CRUD operations
 * and additional query methods for managing admin invitations. It interacts with
 * the database to handle operations related to invitation tokens.
 * </p>
 *
 * <p>This repository is part of the persistence layer for the admin invitation system.</p>
 * @author Lara
 * @author jasmin1707
 */
public interface AdminInvitationRepository extends JpaRepository<AdminInvitation, Long> {

    /**
     * Finds an {@link AdminInvitation} by its token.
     * <p>
     * This method retrieves an invitation record based on the provided token.
     * It is primarily used during the admin registration process to validate the
     * invitation token and check its status.
     * </p>
     *
     * @param token The unique token associated with the {@link AdminInvitation}.
     * @return An {@link Optional} containing the matching {@link AdminInvitation}
     *         if found, or an empty {@link Optional} if no match exists.
     */
    Optional<AdminInvitation> findByToken(String token);
}