package IoTFleetManagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    // Unique identifier for each user, automatically generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Username must be unique and not null
    @Column (unique = true, nullable = false)
    private String username;

    // Password cannot be null
    @Column(nullable = false)
    private String password;

    // Role associated with the user, must be present
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * Default constructor required by JPA
     */
    public User() {
        System.out.println("User entity created with default constructor");
    }

    /**
     * Constructor to create a user with username, password, and role
     *
     * @param username the username of the user
     * @param password the password of the user
     * @param role the role associated with the user
     */
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        System.out.println("User entity created with parameters: username=" + username + ", role=" + role.getName());
    }

    /**
     * Getter for user ID
     *
     * @return the ID of the user
     */
    public Long getId() {
        System.out.println("Getting user ID: " + id);
        return id;
    }

    /**
     * Setter for user ID
     *
     * @param id the ID to set for the user
     */
    public void setId(Long id) {
        System.out.println("Setting user ID: " + id);
        this.id = id;
    }

    /**
     * Getter for username
     *
     * @return the username of the user
     */
    public String getUsername() {
        System.out.println("Getting username: " + username);
        return username;
    }

    /**
     * Setter for username
     *
     * @param username the username to set for the user
     */
    public void setUsername(String username) {
        System.out.println("Setting username: " + username);
        this.username = username;
    }

    /**
     * Getter for password
     *
     * @return the password of the user
     */
    public String getPassword() {
        System.out.println("Getting password for user: " + username);
        return password;
    }

    /**
     * Setter for password
     *
     * @param password the password to set for the user
     */
    public void setPassword(String password) {
        System.out.println("Setting password for user: " + username);
        this.password = password;
    }

    /**
     * Getter for role
     *
     * @return the role of the user
     */
    public Role getRole() {
        System.out.println("Getting role for user: " + username);
        return role;
    }

    /**
     * Setter for role
     *
     * @param role the role to set for the user
     */
    public void setRole(Role role) {
        System.out.println("Setting role for user: " + username + " to role: " + role.getName());
        this.role = role;
    }
}
