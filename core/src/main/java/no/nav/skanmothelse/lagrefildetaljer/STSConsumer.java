package no.nav.skanmothelse.lagrefildetaljer;

import no.nav.skanmothelse.config.properties.SkanmothelseProperties;
import no.nav.skanmothelse.exceptions.functional.SkanmothelseFunctionalException;
import no.nav.skanmothelse.exceptions.technical.SkanmothelseTechnicalException;
import no.nav.skanmothelse.lagrefildetaljer.data.STSResponse;
import no.nav.skanmothelse.metrics.Metrics;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static java.util.Collections.singletonList;
import static no.nav.skanmothelse.config.LocalCacheConfig.STS_CACHE;
import static no.nav.skanmothelse.lagrefildetaljer.RetryConstants.RETRY_DELAY;
import static no.nav.skanmothelse.lagrefildetaljer.RetryConstants.MAX_RETRIES;
import static no.nav.skanmothelse.metrics.MetricLabels.DOK_METRIC;
import static no.nav.skanmothelse.metrics.MetricLabels.PROCESS_NAME;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class STSConsumer {
	private final String urlEncodedBody = "grant_type=client_credentials&scope=openid";

	private final RestTemplate restTemplate;
	private final String stsUrl;

	public STSConsumer(
			RestTemplateBuilder restTemplateBuilder,
			SkanmothelseProperties skanmothelseProperties
	) {

		this.stsUrl = skanmothelseProperties.getStsurl();
		this.restTemplate = restTemplateBuilder
				.basicAuthentication(skanmothelseProperties.getServiceuser().getUsername(),
						skanmothelseProperties.getServiceuser().getPassword())
				.setReadTimeout(Duration.ofMillis(5000L))
				.setConnectTimeout(Duration.ofMillis(5000L))
				.build();
	}

	@Metrics(value = DOK_METRIC, extraTags = {PROCESS_NAME, "getSTSToken"}, percentiles = {0.5, 0.95}, histogram = true)
	@Retryable(maxAttempts = MAX_RETRIES, backoff = @Backoff(delay = RETRY_DELAY))
	@Cacheable(STS_CACHE)
	public STSResponse getSTSToken() {
		try {
			HttpHeaders headers = createHeaders();
			HttpEntity<String> requestEntity = new HttpEntity<>(urlEncodedBody, headers);

			return restTemplate.exchange(stsUrl, POST, requestEntity, STSResponse.class)
					.getBody();

		} catch (HttpClientErrorException e) {
			throw new SkanmothelseFunctionalException(String.format("getSTSToken feilet funksjonelt med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new SkanmothelseTechnicalException(String.format("getSTSToken feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_FORM_URLENCODED);
		headers.setAccept(singletonList(APPLICATION_JSON));
		headers.addAll(NavHeaders.createNavCustomHeaders());
		return headers;
	}
}
