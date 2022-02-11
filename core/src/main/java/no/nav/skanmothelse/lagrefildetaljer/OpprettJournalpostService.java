package no.nav.skanmothelse.lagrefildetaljer;

import no.nav.skanmothelse.lagrefildetaljer.data.OpprettJournalpostRequest;
import no.nav.skanmothelse.lagrefildetaljer.data.OpprettJournalpostResponse;
import no.nav.skanmothelse.lagrefildetaljer.data.STSResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpprettJournalpostService {

    private OpprettJournalpostConsumer opprettJournalpostConsumer;
    private STSConsumer stsConsumer;

    @Autowired
    public OpprettJournalpostService(OpprettJournalpostConsumer opprettJournalpostConsumer, STSConsumer stsConsumer) {
        this.opprettJournalpostConsumer = opprettJournalpostConsumer;
        this.stsConsumer = stsConsumer;
    }

    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request) {
        STSResponse stsResponse = stsConsumer.getSTSToken();
        return opprettJournalpostConsumer.opprettJournalpost(stsResponse.getAccess_token(), request);
    }
}
