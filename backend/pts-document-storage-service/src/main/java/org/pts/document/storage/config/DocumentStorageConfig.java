package org.pts.document.storage.config;

import org.pts.document.storage.config.properties.MinIOProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class DocumentStorageConfig {
    private final MinIOProperties minIOProperties;

    @Bean
    public S3Client s3Client() {
        return S3Client
                .builder()
                .endpointOverride(URI.create(minIOProperties.getUrl()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        minIOProperties.getLogin(),
                                        minIOProperties.getPassword()
                                )
                        )
                ).region(Region.of(minIOProperties.getRegion()))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(minIOProperties.getUrl()))
                .region(Region.of(minIOProperties.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        minIOProperties.getLogin(),
                                        minIOProperties.getPassword()
                                )
                        )
                )
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
