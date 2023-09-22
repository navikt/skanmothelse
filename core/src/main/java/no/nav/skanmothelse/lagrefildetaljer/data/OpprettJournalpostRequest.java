package no.nav.skanmothelse.lagrefildetaljer.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@Builder
@AllArgsConstructor
public class OpprettJournalpostRequest {
    String tittel;
    AvsenderMottaker avsenderMottaker;
    String journalpostType;
    String tema;
    String behandlingstema;
    String kanal;
    String datoMottatt;
    String journalfoerendeEnhet;
    String eksternReferanseId;
    List<Tilleggsopplysning> tilleggsopplysninger;
    Bruker bruker;

    @NotNull(message = "dokumenter kan ikke være null")
    List<Dokument> dokumenter;
}
