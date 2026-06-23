package org.pts.document.storage.config.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "minio")
public class MinIOProperties {
    private String url;
    private String login;
    private String password;
    private String region;
    private Bucket documentPersistenceBucket = new Bucket();
    private Bucket documentTempBucket = new Bucket();
    private SdkConfig sdkConfig = new SdkConfig();
    private ClientConfig clientConfig = new ClientConfig();

    @Setter
    @Getter
    public static class Bucket {
        private String bucketName;
        private long signatureDuration;
    }

    @Setter
    @Getter
    public static class SdkConfig {
        private int apiCallAttemptTimeout;
        private int apiCallTimeout;
    }

    @Setter
    @Getter
    public static class ClientConfig {
        private int connectionTimeout;
        private int readTimeout;
        private int writeTimeout;
    }
}