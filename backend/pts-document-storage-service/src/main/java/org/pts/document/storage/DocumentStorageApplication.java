package org.pts.document.storage;

import org.pts.document.storage.config.properties.DotenvPropertySourceInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DocumentStorageApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DocumentStorageApplication.class);
        app.addInitializers(new DotenvPropertySourceInitializer());
        app.run(args);
    }
}