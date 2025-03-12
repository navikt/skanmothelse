package no.nav.skanmothelse.azure;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "azure")
public record AzureProperties(
		@NotEmpty String openidConfigTokenEndpoint,
		@NotEmpty String appClientId,
		@NotEmpty String appClientSecret
){}