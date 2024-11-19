package IoTFleetManagement.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // For example, "ROLE_USER" or "ROLE_ADMIN"

    /**
     * Default constructor required by JPA
     */
    public Role() {
        System.out.println("Role entity created with default constructor");
    }

    /**
     * Constructor to create a role with a specific name
     *
     * @param name the name of the role
     */
    public Role(String name) {
        this.name = name;
        System.out.println("Role entity created with name: " + name);
    }

    /**
     * Getter for role ID
     *
     * @return the ID of the role
     */
    public Long getId() {
        System.out.println("Getting role ID: " + id);
        return id;
    }

    /**
     * Setter for role ID
     *
     * @param id the ID to set for the role
     */
    public void setId(Long id) {
        System.out.println("Setting role ID: " + id);
        this.id = id;
    }

    /**
     * Getter for role name
     *
     * @return the name of the role
     */
    public String getName() {
        System.out.println("Getting role name: " + name);
        return name;
    }

    /**
     * Setter for role name
     *
     * @param name the name to set for the role
     */
    public void setName(String name) {
        System.out.println("Setting role name: " + name);
        this.name = name;
    }
}
