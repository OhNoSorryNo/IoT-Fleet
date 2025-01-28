package IoTFleetManagement.agent.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a category (tag) that can be associated with one or more {@link Agent} entities.
 * <p>
 * This entity is stored in the database with a unique name and may link to any number of agents
 * via a many-to-many relationship.
 * @author Jasmin1707
 */
@Entity
public class AgentCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique name of this category. This field is required and cannot be {@code null}.
     */
    @Column(unique = true, nullable = false)
    private String name;

    /**
     * A collection of agents associated with this category. This field is marked with {@link JsonIgnore}
     * to avoid circular serialization issues.
     */
    @ManyToMany(mappedBy = "categories")
    @JsonIgnore
    private Set<Agent> agents = new HashSet<>();

    /**
     * Retrieves the primary key identifier for this category.
     *
     * @return the database-generated ID of this category
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the primary key identifier for this category.
     *
     * @param id the database-generated ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retrieves the name of this category.
     *
     * @return a unique, non-null {@link String} representing the category name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this category.
     *
     * @param name a unique {@link String} identifying this category
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the set of {@link Agent} entities associated with this category.
     *
     * @return a {@link Set} of agents assigned to this category
     */
    public Set<Agent> getAgents() {
        return agents;
    }

    /**
     * Sets the collection of {@link Agent} entities for this category.
     *
     * @param agents a {@link Set} of agents to associate with this category
     */
    public void setAgents(Set<Agent> agents) {
        this.agents = agents;
    }
}
