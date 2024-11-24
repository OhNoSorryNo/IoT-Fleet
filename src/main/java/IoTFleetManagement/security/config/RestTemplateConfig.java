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

@Configuration
public class RestTemplateConfig {

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