package no.nav.skanmothelse.config.vault;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.annotation.VaultPropertySource;

@Configuration
@VaultPropertySource(
        value = "${skanmothelse.vault.secretpath}",
        propertyNamePrefix = "skanmothelse.secret.",
        ignoreSecretNotFound = false
)
@ConditionalOnProperty("spring.cloud.vault.enabled")
public class VaultPassphraseConfiguration {

}
