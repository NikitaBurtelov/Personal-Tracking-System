package org.pts.document.storage.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final SecurityProperties properties;

    @Bean
    public SecretKey masterKey() {
        return new SecretKeySpec(
                Base64.getDecoder().decode(properties.getMasterKey()),
                "AES"
        );
    }
}
