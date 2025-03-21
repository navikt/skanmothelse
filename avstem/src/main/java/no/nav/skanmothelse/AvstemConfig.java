package no.nav.skanmothelse;

import no.nav.dok.jiraapi.JiraProperties;
import no.nav.dok.jiraapi.JiraService;
import no.nav.dok.jiraapi.JiraServiceImp;
import no.nav.dok.jiraapi.client.JiraClient;
import no.nav.skanmothelse.config.properties.SkanmothelseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AvstemConfig {

	@Bean
	public JiraService jiraService(JiraClient jiraClient) {
		return new JiraServiceImp(jiraClient);
	}

	@Bean
	public JiraClient jiraClient(SkanmothelseProperties properties) {
		return new JiraClient(jiraProperties(properties));
	}

	public JiraProperties jiraProperties(SkanmothelseProperties properties) {
		SkanmothelseProperties.JiraConfigProperties jira = properties.getJira();
		return JiraProperties.builder()
				.jiraServiceUser(new JiraProperties.JiraServiceUser(jira.getUsername(), jira.getPassword()))
				.url(jira.getUrl())
				.build();
	}
}
