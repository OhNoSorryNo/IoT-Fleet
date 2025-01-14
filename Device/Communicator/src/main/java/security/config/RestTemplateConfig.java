package security.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;

/**
 * Configures RestTemplate with SSL Certificate Pinning.
 * This ensures that the client trusts only the specified certificate (localhost.pem).
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean with SSL Certificate Pinning.
     *
     * @return RestTemplate with SSL pinning.
     * @throws Exception if SSL context initialization fails.
     */
    @Bean
    public RestTemplate restTemplate() throws Exception {
        // Load the PEM certificate from resources
        try (InputStream certInputStream = getClass().getClassLoader().getResourceAsStream("certs/localhost.pem")) {
            if (certInputStream == null) {
                throw new IllegalStateException("Certificate not found in resources/certs/localhost.pem");
            }

            // Create a KeyStore and load the PEM certificate
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("server-cert", CertificateLoader.loadCertificate(certInputStream));

            // Initialize SSL context with the custom KeyStore
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(keyStore, new TrustSelfSignedStrategy())
                    .build();

            // Configure HttpClient to use SSL context
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(socketFactory)
                    .build();

            // Set up RestTemplate with custom HttpClient
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory((HttpClient) httpClient);
            return new RestTemplate(requestFactory);
        }
    }
}
