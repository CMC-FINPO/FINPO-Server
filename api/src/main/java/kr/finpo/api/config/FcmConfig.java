package kr.finpo.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Slf4j
public class FcmConfig {

    @Value("${fcm.private-key}")
    private String fcmPrivateKey;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(fcmPrivateKey);
            InputStream serviceAccount = resource.getInputStream();
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            for (FirebaseApp app : FirebaseApp.getApps()) {
                if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                    return;
                }
            }

            FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException e) {
            log.error("Firebase ServiceAccountKey FileNotFoundException" + e.getMessage());
        } catch (IOException e) {
            log.error("FirebaseOptions IOException" + e.getMessage());
        }
    }
}