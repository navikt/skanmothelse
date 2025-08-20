package no.nav.skanmothelse;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import no.nav.skanmothelse.config.properties.SlackProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

@ComponentScan
@Configuration
public class CoreConfig {

	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("Europe/Oslo");
	public static final ZoneId DEFAULT_ZONE_ID = DEFAULT_TIME_ZONE.toZoneId();

	@Bean
	MethodsClient slackClient(SlackProperties slackProperties) {
		return Slack.getInstance().methods(slackProperties.token());
	}

	@Bean
	Clock clock() {
		return Clock.system(DEFAULT_ZONE_ID);
	}
}
