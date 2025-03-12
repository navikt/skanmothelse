package no.nav.skanmothelse.journalpostapi;

import lombok.extern.slf4j.Slf4j;
import no.nav.skanmothelse.config.properties.SkanmothelseProperties;
import no.nav.skanmothelse.exceptions.functional.SkanmothelseFunctionalException;
import no.nav.skanmothelse.exceptions.technical.SkanmothelseTechnicalException;
import no.nav.skanmothelse.journalpostapi.data.AvstemmingReferanser;
import no.nav.skanmothelse.journalpostapi.data.FeilendeAvstemmingReferanser;
import no.nav.skanmothelse.journalpostapi.data.OpprettJournalpostRequest;
import no.nav.skanmothelse.journalpostapi.data.OpprettJournalpostResponse;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.codec.CodecProperties;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.lang.String.format;
import static no.nav.skanmothelse.azure.AzureOAuthEnabledWebClientConfig.CLIENT_REGISTRATION_DOKARKIV;
import static no.nav.skanmothelse.journalpostapi.NavHeaders.HEADER_NAV_CALL_ID;
import static no.nav.skanmothelse.journalpostapi.RetryConstants.MAX_RETRIES;
import static no.nav.skanmothelse.journalpostapi.RetryConstants.RETRY_DELAY;
import static no.nav.skanmothelse.mdc.MDCConstants.MDC_CALL_ID;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class OpprettJournalpostConsumer {

	private final WebClient webClient;

	public OpprettJournalpostConsumer(
			SkanmothelseProperties skanmothelseProperties,
			WebClient webClient,
			CodecProperties codecProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(skanmothelseProperties.getEndpoints().getDokarkiv().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(configurer -> configurer.defaultCodecs()
								.maxInMemorySize((int) codecProperties.getMaxInMemorySize().toBytes()))
						.build())
				.build();
	}

	@Retryable(retryFor = SkanmothelseTechnicalException.class, maxAttempts = MAX_RETRIES, backoff = @Backoff(delay = RETRY_DELAY))
	public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest opprettJournalpostRequest) {
		return webClient.post()
				.uri("/journalpost?foersoekFerdigstill=false")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.header(HEADER_NAV_CALL_ID, MDC.get(MDC_CALL_ID))
				.bodyValue(opprettJournalpostRequest)
				.retrieve()
				.bodyToMono(OpprettJournalpostResponse.class)
				.onErrorMap(error -> mapOpprettJournalpostError(error, opprettJournalpostRequest.getEksternReferanseId()))
				.block();

	}

	@Retryable(retryFor = SkanmothelseTechnicalException.class, maxAttempts = MAX_RETRIES, backoff = @Backoff(delay = RETRY_DELAY))
	public FeilendeAvstemmingReferanser feilendeAvstemmingReferanser(AvstemmingReferanser avstemmingReferanser) {
		return webClient.post()
				.uri("/avstemReferanser")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.header(HEADER_NAV_CALL_ID, MDC.get(MDC_CALL_ID))
				.bodyValue(avstemmingReferanser)
				.retrieve()
				.bodyToMono(FeilendeAvstemmingReferanser.class)
				.onErrorMap(this::mapAvstemReferanserError)
				.block();
	}

	private Throwable mapOpprettJournalpostError(Throwable error, String eksternReferanseId) {
		if (error instanceof WebClientResponseException webException && webException.getStatusCode().is4xxClientError()) {
			if (webException instanceof WebClientResponseException.Conflict conflict) {
				OpprettJournalpostResponse journalpost = conflict.getResponseBodyAs(OpprettJournalpostResponse.class);
				log.info("Det eksisterer allerede en journalpost i dokarkiv med fil={}. Denne har journalpostId={}. Oppretter ikke ny journalpost.",
						eksternReferanseId, journalpost.journalpostId());
				return new SkanmothelseFunctionalException(format("Det eksisterer allerede en journalpost i dokarkiv med eksternReferanseId={}, journalpostId={}. Feilmelding=%s",
						eksternReferanseId, journalpost.journalpostId(), webException.getMessage()), webException);
			}
			return new SkanmothelseFunctionalException(format("opprettJournalpost feilet funksjonelt med statusKode=%s. Feilmelding=%s", webException
					.getStatusCode(), webException.getMessage()), webException);
		}
		return new SkanmothelseTechnicalException(format("opprettJournalpost feilet teknisk med feilmelding=%s", error.getMessage()), error);
	}

	private Throwable mapAvstemReferanserError(Throwable error) {
		if (error instanceof WebClientResponseException webException && webException.getStatusCode().is4xxClientError()) {
			return new SkanmothelseFunctionalException(format("avstemReferanser feilet funksjonelt med statusKode=%s. Feilmelding=%s",
					webException.getStatusCode(), webException.getMessage()), webException);
		}
		return new SkanmothelseTechnicalException(format("avstemReferanser feilet teknisk med Feilmelding=%s", error.getMessage()), error);
	}
}
