package no.nav.skanmothelse.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@ConfigurationProperties("skanmothelse.vault")
@Validated
public class SkanmothelseVaultProperties {

    @NotBlank
    private String backend;

    @NotBlank
    private String kubernetespath;

}