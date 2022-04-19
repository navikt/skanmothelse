package no.nav.skanmothelse.helse;

import lombok.extern.slf4j.Slf4j;
import no.nav.skanmothelse.exceptions.technical.SkanmothelseTechnicalException;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static no.nav.skanmothelse.helse.PostboksHelseRoute.PROPERTY_FORSENDELSE_ZIPNAME;
import static org.apache.camel.Exchange.AGGREGATED_CORRELATION_KEY;
import static org.apache.camel.Exchange.FILE_NAME;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;


@Slf4j
public class PostboksHelseSkanningAggregator implements AggregationStrategy {
    public static final String XML_EXTENSION = "xml";
    public static final String OCR_EXTENSION = "ocr";
    public static final String PDF_EXTENSION = "pdf";

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        try {
            if (oldExchange == null) {
                final PostboksHelseEnvelope envelope = new PostboksHelseEnvelope(newExchange.getProperty(PROPERTY_FORSENDELSE_ZIPNAME, String.class), getBaseName(newExchange.getIn().getHeader(FILE_NAME, String.class)));
                applyOnEnvelope(newExchange, envelope);
                newExchange.getIn().setBody(envelope);
                return newExchange;
            }

            final PostboksHelseEnvelope envelope = oldExchange.getIn().getBody(PostboksHelseEnvelope.class);
            applyOnEnvelope(newExchange, envelope);
            return oldExchange;
        } catch (IOException e) {
            throw new SkanmothelseTechnicalException("Klarte ikke lese fil", e);
        }
    }

    @Override
    public void timeout(Exchange exchange, int index, int total, long timeout) {
        final String fil = exchange.getProperty(AGGREGATED_CORRELATION_KEY, String.class);
        log.info("Skanmothelse fant ikke 3 filer under aggreggering av zipfil innen timeout={}ms. Fortsetter behandling. fil={}.", timeout, fil);
    }

    private void applyOnEnvelope(Exchange newExchange, PostboksHelseEnvelope envelope) throws IOException {
        final String extension = getExtension(newExchange.getIn().getHeader(FILE_NAME, String.class));
        if (XML_EXTENSION.equals(extension)) {
            final InputStream inputStream = newExchange.getIn().getBody(InputStream.class);
            final byte[] xml = IOUtils.toByteArray(inputStream);
            envelope.setXml(xml);
        } else if (OCR_EXTENSION.equals(extension)) {
            final InputStream inputStream = newExchange.getIn().getBody(InputStream.class);
            envelope.setOcr(IOUtils.toByteArray(inputStream));
        } else if (PDF_EXTENSION.equals(extension)) {
            final InputStream inputStream = newExchange.getIn().getBody(InputStream.class);
            envelope.setPdf(IOUtils.toByteArray(inputStream));
        }
    }
}
