package no.nav.skanmothelse;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import no.nav.skanmothelse.config.properties.SkanmothelseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan
@Configuration
public class CoreConfig {

	@Bean
	MethodsClient slackClient(SkanmothelseProperties skanmothelseProperties) {
		return Slack.getInstance().methods(skanmothelseProperties.getSlack().getToken());
	}
}
