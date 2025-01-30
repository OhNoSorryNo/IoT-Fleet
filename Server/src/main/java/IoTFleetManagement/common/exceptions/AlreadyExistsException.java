package IoTFleetManagement.common.exceptions;

/**
 * Exception thrown to indicate that an entity already exists.
 *
 * <p>This exception is typically used when attempting to create or register an entity
 * that conflicts with an existing entity in the system. It extends {@link RuntimeException},
 * allowing it to be used as an unchecked exception.</p>
 *
 * @author jasmin1707
 * @author Lara
 */
public class AlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new {@link AlreadyExistsException} with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public AlreadyExistsException(String message) {
        super(message);
    }
}
