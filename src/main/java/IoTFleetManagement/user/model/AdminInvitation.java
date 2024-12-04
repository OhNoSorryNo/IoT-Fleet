package IoTFleetManagement.user.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an admin invitation.
 * <p>
 * This class maps to the "admin_invitations" table in the database. It is used to manage
 * invitation tokens that allow users to register as admins. Each token is unique,
 * time-limited, and can only be used once.
 * </p>
 *
 * <p><b>Fields:</b></p>
 * <ul>
 *   <li><strong>id:</strong> Primary key for the invitation record.</li>
 *   <li><strong>token:</strong> Unique invitation token used for admin registration.</li>
 *   <li><strong>used:</strong> Flag indicating whether the token has been used.</li>
 *   <li><strong>createdAt:</strong> Timestamp indicating when the token was created.</li>
 *   <li><strong>expiresAt:</strong> Timestamp indicating when the token will expire.</li>
 *   <li><strong>usedAt:</strong> Timestamp indicating when the token was used (if applicable).</li>
 * </ul>
 *
 * <p><b>Constraints:</b></p>
 * <ul>
 *   <li>The <strong>token</strong> must be unique and cannot be null.</li>
 *   <li>The <strong>createdAt</strong> and <strong>expiresAt</strong> fields are mandatory.</li>
 *   <li>The <strong>used</strong> flag defaults to {@code false} and is updated upon token usage.</li>
 * </ul>
 *
 * @author jasmin1707
 * @author Lara
 */
@Entity
@Table(name = "admin_invitations")
public class AdminInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private boolean used = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime usedAt;

    /**
     * Gets the unique identifier for this invitation.
     *
     * @return The unique identifier (ID) of the invitation.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this invitation.
     *
     * @param id The unique identifier (ID) to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the invitation token.
     *
     * @return The unique invitation token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the invitation token.
     *
     * @param token The unique invitation token to set.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Checks if the token has been used.
     *
     * @return {@code true} if the token has been used, {@code false} otherwise.
     */
    public boolean isUsed() {
        return used;
    }

    /**
     * Sets the status of whether the token has been used.
     *
     * @param used {@code true} if the token has been used, {@code false} otherwise.
     */
    public void setUsed(boolean used) {
        this.used = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the expiration timestamp of the token.
     *
     * @return The expiration timestamp of the token.
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the expiration timestamp of the token.
     *
     * @param expiresAt The expiration timestamp of the token.
     */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    /**
     * Sets the timestamp of when the token was used.
     *
     * @param usedAt The timestamp of token usage.
     */
    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}
