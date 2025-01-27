package app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
public class RestTemplateConfig {

    /**
     * Creates and configures a RestTemplate bean with a custom SSLContext
     * that disables SSL certificate validation for all HTTPS requests.
     */
    @Bean
    public RestTemplate restTemplate() throws Exception {

        // TrustManager that does not perform any checks on certificates.
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        // No client certificate validation
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        // No server certificate validation
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0]; // No accepted issuers
                    }
                }
        };

        // Create an SSLContext with the TrustManager that skips validation.
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        // Disable hostname verification for HTTPS connections.
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        // Set the custom SSLContext for all HTTPS connections.
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        // Return a new RestTemplate instance.
        return new RestTemplate();
    }
}
