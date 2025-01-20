package IoTFleetManagement.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyStore;

/**
 * Configuration class for creating a {@link RestTemplate} bean with custom SSL configuration.
 * <p>
 * This configuration bypasses SSL certificate validation and hostname verification, making it
 * suitable for development or testing environments where trusted certificates are not required.
 * However, this approach is insecure and should not be used in production environments.
 *
 * @author Lara
 * @author Jasmin1707
 */
@Configuration
public class RestTemplateConfig {

    @Value("${keystore.password}")
    private String keystorePassword;


    /**
     * Creates a {@link RestTemplate} bean with a custom SSL context that trusts all certificates.
     * <p>
     * The {@link RestTemplate} is configured with a {@link SimpleClientHttpRequestFactory} that uses
     * an {@link SSLContext} allowing all SSL certificates and a hostname verifier that accepts all hostnames.
     * </p>
     *
     * @return a configured {@link RestTemplate} instance
     * @throws Exception if an error occurs while initializing the SSL context
     */
    @Bean
    public RestTemplate restTemplate() throws Exception {
        // Load the keystore from the resources folder
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keystoreStream = getClass().getClassLoader().getResourceAsStream("keystoreOld.p12")) {
            if (keystoreStream == null) {
                throw new IllegalStateException("Keystore not found in resources folder");
            }
            keyStore.load(keystoreStream, keystorePassword.toCharArray());
        }

        // Create a TrustManagerFactory for the keystore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        // Create an SSLContext using the keystore's TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());

        // Configure RestTemplate to use the custom SSLContext
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected @NonNull HttpURLConnection openConnection(@NonNull URL url, @Nullable Proxy proxy) throws IOException {
                HttpURLConnection connection = super.openConnection(url, proxy);
                if (connection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
                }
                return connection;
            }
        };

        return new RestTemplate(requestFactory);
    }
}