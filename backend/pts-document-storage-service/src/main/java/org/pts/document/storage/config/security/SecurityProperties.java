package org.pts.document.storage.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Deprecated
@Setter
@Getter
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private String masterKey;
}