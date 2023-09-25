package no.nav.skanmothelse.helse;

import no.nav.skanmothelse.exceptions.functional.ForsendelseNotCompleteException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static no.nav.skanmothelse.helse.PostboksHelseEnvelopeTestObjects.FILEBASENAME;
import static no.nav.skanmothelse.helse.PostboksHelseEnvelopeTestObjects.OCR_FIL;
import static no.nav.skanmothelse.helse.PostboksHelseEnvelopeTestObjects.PDF_FIL;
import static no.nav.skanmothelse.helse.PostboksHelseEnvelopeTestObjects.XML_FIL;
import static no.nav.skanmothelse.helse.PostboksHelseEnvelopeTestObjects.ZIPNAME;
import static no.nav.skanmothelse.helse.PostboksHelseEnvelopeTestObjects.createBaseEnvelope;
import static no.nav.skanmothelse.helse.PostboksHelseEnvelopeTestObjects.createEnvelopeWithOcr;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


class PostboksHelseEnvelopeTest {

    @Test
    void shouldThrowExceptionWhenValidateNoXml() {
        assertThatExceptionOfType(ForsendelseNotCompleteException.class)
                .isThrownBy(() -> createBaseEnvelope().xml(null).build().validate())
                .withMessage("Fant ikke filnavn=" + FILEBASENAME + ".xml i zip=" + ZIPNAME);
    }

    @Test
    void shouldThrowExceptionWhenValidateNoPdf() {
        assertThatExceptionOfType(ForsendelseNotCompleteException.class)
                .isThrownBy( () -> createBaseEnvelope().pdf(null).build().validate())
                .withMessage("Fant ikke filnavn=" + FILEBASENAME + ".pdf i zip=" + ZIPNAME);
    }

    @Test
    void shouldCreateZip() throws IOException, ArchiveException {
        final PostboksHelseEnvelope envelope = createEnvelopeWithOcr();
        ByteArrayInputStream zip = (ByteArrayInputStream) envelope.createZip();
        SeekableInMemoryByteChannel inMemoryByteChannel = new SeekableInMemoryByteChannel(zip.readAllBytes());
        ZipFile zipFile = new ZipFile(inMemoryByteChannel);
        assertThat(readEntry(zipFile, FILEBASENAME + ".xml")).containsExactly(XML_FIL);
        assertThat(readEntry(zipFile, FILEBASENAME + ".pdf")).containsExactly(PDF_FIL);
        assertThat(readEntry(zipFile, FILEBASENAME + ".ocr")).containsExactly(OCR_FIL);
    }

    private byte[] readEntry(final ZipFile zipFile, final String name) throws IOException {
        ZipArchiveEntry archiveEntry = zipFile.getEntry(name);
        InputStream inputStream = zipFile.getInputStream(archiveEntry);
        return IOUtils.toByteArray(inputStream);
    }
}