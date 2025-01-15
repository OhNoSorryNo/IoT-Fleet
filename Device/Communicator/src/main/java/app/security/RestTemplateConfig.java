package app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.HttpsURLConnection;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() throws Exception {
        // 1) PEM-Zertifikat laden
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        FileInputStream fis = new FileInputStream("/app/certs/server_cert.pem");
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(fis);

        // 2) KeyStore mit dem geladenen Zertifikat erstellen
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("caCert", caCert);

        // 3) TrustManagerFactory mit dem KeyStore initialisieren
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        // 4) SSLContext mit TrustManagerFactory konfigurieren
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        // 5) SSL-Kontext global setzen
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        // 6) Einfache RestTemplate-Instanz zurückgeben
        return new RestTemplate();
    }
}
