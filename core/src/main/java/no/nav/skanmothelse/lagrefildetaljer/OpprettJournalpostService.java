package no.nav.skanmothelse.lagrefildetaljer;

import no.nav.skanmothelse.lagrefildetaljer.data.OpprettJournalpostRequest;
import no.nav.skanmothelse.lagrefildetaljer.data.OpprettJournalpostResponse;
import no.nav.skanmothelse.lagrefildetaljer.data.STSResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class OpprettJournalpostService {

    private OpprettJournalpostConsumer opprettJournalpostConsumer;
    private STSConsumer stsConsumer;

    @Inject
    public OpprettJournalpostService(OpprettJournalpostConsumer opprettJournalpostConsumer, STSConsumer stsConsumer) {
        this.opprettJournalpostConsumer = opprettJournalpostConsumer;
        this.stsConsumer = stsConsumer;
    }

    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request) {
        STSResponse stsResponse = stsConsumer.getSTSToken();
        return opprettJournalpostConsumer.opprettJournalpost(stsResponse.getAccess_token(), request);
    }
}
