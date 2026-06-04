package org.pts.document.storage.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Setter
@Getter
public class MinIOProperties {
    private String url;
    private String login;
    private String password;
    private String region;
    private Img img = new Img();

    @Setter
    @Getter
    public static class Img {
        private String bucketName;
        private long signatureDuration;
    }
}