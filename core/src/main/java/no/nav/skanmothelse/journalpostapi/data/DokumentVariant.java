package no.nav.skanmothelse.journalpostapi.data;

import lombok.Builder;

@Builder
public record DokumentVariant(
		String filtype,
		byte[] fysiskDokument,
		String variantformat,
		String filnavn,
		String batchnavn) {
}
