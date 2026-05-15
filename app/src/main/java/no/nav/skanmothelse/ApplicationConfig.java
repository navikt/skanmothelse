package no.nav.skanmothelse;

import no.nav.skanmothelse.azure.AzureProperties;
import no.nav.skanmothelse.config.properties.JiraAuthProperties;
import no.nav.skanmothelse.config.properties.SkanmothelseProperties;
import no.nav.skanmothelse.config.properties.SlackProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;

@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties({
		SkanmothelseProperties.class,
		SlackProperties.class,
		JiraAuthProperties.class,
		AzureProperties.class
})
@EnableResilientMethods
@Configuration
public class ApplicationConfig {
}
