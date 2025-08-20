package no.nav.skanmothelse;

import no.nav.dok.jiraapi.JiraProperties;
import no.nav.dok.jiraapi.JiraProperties.JiraServiceUser;
import no.nav.dok.jiraapi.JiraService;
import no.nav.dok.jiraapi.client.JiraClient;
import no.nav.skanmothelse.config.properties.JiraAuthProperties;
import no.nav.skanmothelse.config.properties.SkanmothelseProperties;
import no.nav.skanmothelse.config.properties.SkanmothelseProperties.JiraConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AvstemConfig {

	@Bean
	public JiraService jiraService(JiraClient jiraClient) {
		return new JiraService(jiraClient);
	}

	@Bean
	public JiraClient jiraClient(SkanmothelseProperties properties, JiraAuthProperties jiraAuthProperties) {
		return new JiraClient(jiraProperties(properties, jiraAuthProperties));
	}

	public JiraProperties jiraProperties(SkanmothelseProperties properties, JiraAuthProperties jiraAuthProperties) {
		JiraConfigProperties jira = properties.getJira();

		return JiraProperties.builder()
				.jiraServiceUser(new JiraServiceUser(jiraAuthProperties.username(), jiraAuthProperties.password()))
				.url(jira.getUrl())
				.build();
	}
}
