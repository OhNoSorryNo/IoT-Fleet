package security.config;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Utility class to load X.509 certificates from an InputStream.
 */
public class CertificateLoader {

    /**
     * Loads an X.509 certificate from the provided InputStream.
     *
     * @param inputStream InputStream of the certificate (PEM format)
     * @return X509Certificate object
     * @throws Exception if the certificate cannot be loaded
     */
    public static X509Certificate loadCertificate(InputStream inputStream) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(inputStream);
    }
}
