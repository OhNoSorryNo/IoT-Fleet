package IoTFleetManagement.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

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
        // Create an SSLContext that trusts all certificates
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{ new X509TrustManager(){
            @Override
            public X509Certificate[] getAcceptedIssuers(){ return new X509Certificate[0]; }
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType){}
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType){}
        }}, new java.security.SecureRandom());

        // Create a custom request factory that uses our SSLContext
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory(){
            @Override
            protected HttpURLConnection openConnection(URL url, java.net.Proxy proxy) throws IOException {
                HttpURLConnection connection = super.openConnection(url, proxy);
                if(connection instanceof HttpsURLConnection){
                    ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
                    ((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier(){
                        @Override
                        public boolean verify(String hostname, SSLSession session){
                            return true;
                        }
                    });
                }
                return connection;
            }
        };

        // Create and return the RestTemplate
        return new RestTemplate(requestFactory);
    }
}