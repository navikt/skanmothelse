package no.nav.skanmothelse.helse;

import lombok.extern.slf4j.Slf4j;
import no.nav.skanmothelse.journalpostapi.OpprettJournalpostConsumer;
import no.nav.skanmothelse.journalpostapi.data.OpprettJournalpostRequest;
import no.nav.skanmothelse.journalpostapi.data.OpprettJournalpostResponse;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class PostboksHelseService {
	private final OpprettJournalpostPostboksHelseRequestMapper mapper;
	private OpprettJournalpostConsumer opprettJournalpostConsumer;

	@Autowired
	public PostboksHelseService(OpprettJournalpostPostboksHelseRequestMapper mapper,
								OpprettJournalpostConsumer opprettJournalpostConsumer) {
		this.mapper = mapper;
		this.opprettJournalpostConsumer = opprettJournalpostConsumer;
	}

	@Handler
	public String behandleForsendelse(@Body PostboksHelseEnvelope envelope) {
		if (envelope.getOcr() == null) {
			log.info("Skanmothelse mangler OCR fil. Fortsetter journalføring. fil=" + envelope.getFilebasename() + ", batch=" + envelope.getSkanningmetadata().getJournalpost().getBatchnavn());
		}
		OpprettJournalpostRequest request = mapper.mapRequest(envelope);
		final OpprettJournalpostResponse opprettJournalpostResponse = opprettJournalpostConsumer.opprettJournalpost(request);
		return opprettJournalpostResponse.journalpostId();
	}
}
