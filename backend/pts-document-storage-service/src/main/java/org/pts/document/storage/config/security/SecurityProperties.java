package org.pts.document.storage.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;

@Setter
@Getter
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private SecretKey masterKey;
}