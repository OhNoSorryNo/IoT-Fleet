package app;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"app", "service", "controller", "model"})
@EnableScheduling

public class CommunicatorMain {
    public static void main(String[] args) {
        SpringApplication.run(CommunicatorMain.class, args);
    }
}
