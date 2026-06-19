package org.pts.document.storage.config.app.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("document.storage")
public class DocumentStorageProperties {
}
