package no.nav.skanmothelse.helse;

import lombok.extern.slf4j.Slf4j;
import no.nav.skanmothelse.lagrefildetaljer.OpprettJournalpostService;
import no.nav.skanmothelse.lagrefildetaljer.data.OpprettJournalpostRequest;
import no.nav.skanmothelse.lagrefildetaljer.data.OpprettJournalpostResponse;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.stereotype.Component;

import javax.inject.Inject;


@Slf4j
@Component
public class PostboksHelseService {
    private final OpprettJournalpostPostboksHelseRequestMapper mapper;
    private final OpprettJournalpostService opprettJournalpostService;

    @Inject
    public PostboksHelseService(OpprettJournalpostPostboksHelseRequestMapper mapper,
                                OpprettJournalpostService opprettJournalpostService) {
        this.mapper = mapper;
        this.opprettJournalpostService = opprettJournalpostService;
    }

    @Handler
    public String behandleForsendelse(@Body PostboksHelseEnvelope envelope) {
        if (envelope.getOcr() == null) {
            log.info("Skanmothelse mangler OCR fil. Fortsetter journalføring. fil=" + envelope.getFilebasename() + ", batch=" + envelope.getSkanningmetadata().getJournalpost().getBatchnavn());
        }
        OpprettJournalpostRequest request = mapper.mapRequest(envelope);
        final OpprettJournalpostResponse opprettJournalpostResponse = opprettJournalpostService.opprettJournalpost(request);
        return opprettJournalpostResponse.getJournalpostId();
    }
}
