package org.pts.document.storage.config.minio;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class MinIOStorageConfig {
    private final MinIOProperties minIOProperties;

    @Bean
    public S3Client s3Client() {
        var sdkConfig = minIOProperties.getSdkConfig();
        var clientConfig = minIOProperties.getClientConfig();

        var config = ClientOverrideConfiguration.builder()
                .apiCallAttemptTimeout(
                        Duration.ofSeconds(
                                sdkConfig.getApiCallAttemptTimeout()
                        )
                )
                .apiCallTimeout(
                        Duration.ofSeconds(
                                sdkConfig.getApiCallTimeout()
                        )
                )
                .build();

        var httpClient = ApacheHttpClient.builder()
                .connectionTimeout(
                        Duration.ofSeconds(
                                clientConfig.getConnectionTimeout()
                        )
                )
                .socketTimeout(
                        Duration.ofSeconds(
                                clientConfig.getReadTimeout()
                        )
                )
                .build();

        return S3Client.builder()
                .httpClient(httpClient)
                .endpointOverride(URI.create(minIOProperties.getUrl()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        minIOProperties.getLogin(),
                                        minIOProperties.getPassword()
                                )
                        )
                ).region(Region.of(minIOProperties.getRegion()))
                .overrideConfiguration(config)
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
