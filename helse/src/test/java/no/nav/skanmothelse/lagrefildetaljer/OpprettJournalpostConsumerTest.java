package no.nav.skanmothelse.lagrefildetaljer;

import no.nav.skanmothelse.exceptions.functional.SkanmothelseFunctionalException;
import no.nav.skanmothelse.helse.AbstractIt;
import no.nav.skanmothelse.lagrefildetaljer.data.OpprettJournalpostRequest;
import no.nav.skanmothelse.lagrefildetaljer.data.OpprettJournalpostResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.EnableRetry;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@EnableRetry
class OpprettJournalpostConsumerTest extends AbstractIt {

	@Autowired
	OpprettJournalpostConsumer opprettJournalpostConsumer;

	@Test
	public void shouldGetJournalpostWhenResponseIs () {
		stubOpprettJournalpostResponseConflictWithValidResponse();
		OpprettJournalpostRequest request = OpprettJournalpostRequest.builder().build();

		OpprettJournalpostResponse response = opprettJournalpostConsumer.opprettJournalpost("token", request);
		assertEquals("567010363", response.journalpostId());
	}

	@Test
	public void shouldNotGetJournalpostWhenConflictDoesNotCorrectHaveBody() {
		stubOpprettJournalpostResponseConflictWithInvalidResponse();

		assertThrows(
				SkanmothelseFunctionalException.class,
				() -> opprettJournalpostConsumer.opprettJournalpost("token", null)
		);
		verify(exactly(5), postRequestedFor(urlMatching("/rest/journalpostapi/v1/journalpost\\?foersoekFerdigstill=false")));
	}

}