package org.example.relief.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component            // picked up by component-scan
public class FireBaseConfig {

    @PostConstruct
    public void init() throws Exception {
        if (!FirebaseApp.getApps().isEmpty()) {   // already initialised
            return;
        }

        // ① put your service-account JSON in src/main/resources
        var serviceAcc = new ClassPathResource("firebase-service-account.json")
                .getInputStream();

        FirebaseOptions opts = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAcc))
                .build();

        FirebaseApp.initializeApp(opts);          // registers the DEFAULT app
        System.out.println("✅ Firebase DEFAULT app initialised");
    }
}
