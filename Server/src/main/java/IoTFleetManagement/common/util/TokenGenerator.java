package IoTFleetManagement.common.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating secure random tokens.
 * <p>
 * This class provides a method to generate cryptographically secure random tokens
 * encoded as URL-safe Base64 strings. These tokens are used for invitation codes.
 * </p>
 *
 *
 * @author Jasmin1707
 * @author Lara
 */
public class TokenGenerator {

    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates a secure random token encoded as a URL-safe Base64 string.
     * <p>
     * The generated token is 192 bits (24 bytes) long, providing a high level of security
     * and uniqueness. It is encoded using Base64 without padding to ensure it is URL-safe
     * and can be transmitted without additional encoding.
     * </p>
     *
     * @return A cryptographically secure, unique token as a URL-safe Base64 string.
     *
     * @throws IllegalStateException If the SecureRandom instance fails to generate random bytes.
     * @author Jasmin1707
     * @author Lara
     */
    public static String generateToken() {
        byte[] bytes = new byte[24]; // 24 bytes = 192 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
