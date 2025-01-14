package security.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Configures RestTemplate with SSL Certificate Pinning using a PEM certificate.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean using PEM certificate for SSL pinning.
     *
     * @return RestTemplate with SSL pinning.
     * @throws Exception if SSL context initialization fails.
     */
    @Bean
    public RestTemplate restTemplate() throws Exception {
        try (InputStream certInputStream = new FileInputStream("/app/certs/server_cert.pem")) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(certInputStream);

            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> {
                        for (X509Certificate cert : chain) {
                            if (cert.equals(certificate)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .build();

            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(socketFactory)
                    .build();

            return new RestTemplate(new HttpComponentsClientHttpRequestFactory((HttpClient) httpClient));
        }
    }

}
