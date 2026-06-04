package org.pts.document.storage.config.properties;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;
import java.util.stream.Collectors;

public class DotenvPropertySourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir") + "/backend/pts-document-storage-service")
                .filename(".env")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        if (!dotenv.entries().isEmpty()) {
            Map<String, Object> map = dotenv.entries()
                    .stream()
                    .collect(
                            Collectors.toMap(
                                    DotenvEntry::getKey,
                                    DotenvEntry::getValue
                            )
                    );
            applicationContext.getEnvironment()
                    .getPropertySources()
                    .addFirst(
                            new MapPropertySource("dotenvProperties", map)
                    );
        }
    }
}
