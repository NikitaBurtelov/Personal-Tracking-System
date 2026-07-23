package org.pts.document.storage.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vault.transit")
@Getter
@Setter
public class VaultTransitProperties {
    private VaultKey documentEncryption = new VaultKey();

    @Getter
    @Setter
    public static class VaultKey {
        private String key;
    }
}
