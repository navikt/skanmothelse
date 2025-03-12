package no.nav.skanmothelse.journalpostapi.data;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DokumentVariant {
	String filtype;
	byte[] fysiskDokument;
	String variantformat;
	String filnavn;
	String batchnavn;
}
