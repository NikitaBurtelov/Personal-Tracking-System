package org.pts.document.storage.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

@Configuration
public class VaultTransitConfig {
    @Value("${spring.cloud.vault.uri}")
    private String vaultUri;

    @Profile({"dev", "test"})
    @Bean
    public VaultTemplate vaultTemplate() {

        VaultEndpoint endpoint =
                VaultEndpoint.from(vaultUri);

        ClientAuthentication authentication =
                new TokenAuthentication("root");

        return new VaultTemplate(
                endpoint,
                authentication
        );
    }
}