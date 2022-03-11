package no.nav.skanmothelse.helse.decrypt;

import lombok.extern.slf4j.Slf4j;
import no.nav.skanmothelse.config.properties.SkanmothelseProperties;
import no.nav.skanmothelse.exceptions.functional.AbstractSkanmothelseFunctionalException;
import no.nav.skanmothelse.helse.ErrorMetricsProcessor;
import no.nav.skanmothelse.helse.MdcRemoverProcessor;
import no.nav.skanmothelse.helse.MdcSetterProcessor;
import no.nav.skanmothelse.helse.PostboksHelseEnvelope;
import no.nav.skanmothelse.helse.PostboksHelseService;
import no.nav.skanmothelse.helse.PostboksHelseSkanningAggregator;
import no.nav.skanmothelse.helse.SkanningmetadataCounter;
import no.nav.skanmothelse.helse.SkanningmetadataUnmarshaller;
import no.nav.skanmothelse.metrics.DokCounter;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.bouncycastle.openpgp.PGPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static no.nav.skanmothelse.metrics.DokCounter.DOMAIN;
import static no.nav.skanmothelse.metrics.DokCounter.HELSE;
import static org.apache.camel.Exchange.FILE_NAME;
import static org.apache.camel.Exchange.FILE_NAME_PRODUCED;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.WARN;

@Slf4j
@Component
public class PostboksHelseRoutePGPEncrypted extends RouteBuilder {
	public static final String PROPERTY_FORSENDELSE_ZIPNAME = "ForsendelseZipname";
	public static final String PROPERTY_FORSENDELSE_BATCHNAVN = "ForsendelseBatchNavn";
	public static final String PROPERTY_FORSENDELSE_FILEBASENAME = "ForsendelseFileBasename";
	public static final String KEY_LOGGING_INFO = "fil=${exchangeProperty." + PROPERTY_FORSENDELSE_FILEBASENAME + "}, batch=${exchangeProperty." + PROPERTY_FORSENDELSE_BATCHNAVN + "}";
	static final int FORVENTET_ANTALL_PER_FORSENDELSE = 2;

	private final SkanmothelseProperties skanmothelseProperties;
	private final PostboksHelseService postboksHelseService;
	private final PgpDecryptService pgpDecryptService;

	@Autowired
	public PostboksHelseRoutePGPEncrypted(
			SkanmothelseProperties skanmothelseProperties,
			PostboksHelseService postboksHelseService,
			PgpDecryptService pgpDecryptService) {
		this.skanmothelseProperties = skanmothelseProperties;
		this.postboksHelseService = postboksHelseService;
		this.pgpDecryptService = pgpDecryptService;
	}

	@Override
	public void configure() {
		String PGP_AVVIK = "direct:pgp_encrypted_avvik_helse";
		String PROCESS_PGP_ENCRYPTED = "direct:pgp_encrypted_process_helse";

		// @formatter:off
		onException(Exception.class)
				.handled(true)
				.process(new MdcSetterProcessor())
				.process(new ErrorMetricsProcessor())
				.log(ERROR, log, "Skanmothelse-pgp feilet teknisk for " + KEY_LOGGING_INFO + ". ${exception}")
				.setHeader(FILE_NAME, simple("${exchangeProperty." + PROPERTY_FORSENDELSE_BATCHNAVN + "}/${exchangeProperty." + PROPERTY_FORSENDELSE_FILEBASENAME + "}-teknisk.zip"))
				.to(PGP_AVVIK)
				.log(ERROR, log, "Skanmothelse-pgp skrev feiletzip=${header." + FILE_NAME_PRODUCED + "} til feilmappe. " + KEY_LOGGING_INFO + ".");

		// Får ikke dekryptert .zip.pgp - mest sannsynlig mismatch mellom private key og public key
		onException(PGPException.class)
				.handled(true)
				.process(new MdcSetterProcessor())
				.process(new ErrorMetricsProcessor())
				.log(ERROR, log, "Skanmothelse-pgp feilet i dekryptering av .zip.pgp for " + KEY_LOGGING_INFO + ". ${exception}")
				.setHeader(FILE_NAME, simple("${exchangeProperty." + PROPERTY_FORSENDELSE_BATCHNAVN + "}${exchangeProperty." + PROPERTY_FORSENDELSE_FILEBASENAME + "}.zip.pgp"))
				.to("{{skanmothelse.helse.endpointuri}}/{{skanmothelse.helse.filomraade.feilmappe}}" +
						"?{{skanmothelse.helse.endpointconfig}}")
				.log(ERROR, log, "Skanmothelse-pgp skrev feiletzip=${header." + FILE_NAME_PRODUCED + "} til feilmappe. " + KEY_LOGGING_INFO + ".")
				.end()
				.process(new MdcRemoverProcessor());

		// Kjente funksjonelle feil
		onException(AbstractSkanmothelseFunctionalException.class)
				.handled(true)
				.process(new MdcSetterProcessor())
				.process(new ErrorMetricsProcessor())
				.log(WARN, log, "Skanmothelse-pgp feilet funksjonelt for " + KEY_LOGGING_INFO + ". ${exception}")
				.setHeader(FILE_NAME, simple("${exchangeProperty." + PROPERTY_FORSENDELSE_BATCHNAVN + "}/${exchangeProperty." + PROPERTY_FORSENDELSE_FILEBASENAME + "}.zip"))
				.to(PGP_AVVIK)
				.log(WARN, log, "Skanmothelse-pgp skrev feiletzip=${header." + FILE_NAME_PRODUCED + "} til feilmappe. " + KEY_LOGGING_INFO + ".");

		from("{{skanmothelse.helse.endpointuri}}/{{skanmothelse.helse.filomraade.inngaaendemappe}}" +
				"?{{skanmothelse.helse.endpointconfig}}" +
				"&delay=" + TimeUnit.SECONDS.toMillis(60) +
				"&antInclude=*.zip.pgp,*.ZIP.pgp" +
				"&initialDelay=1000" +
				"&maxMessagesPerPoll=10" +
				"&move=processed" +
				"&scheduler=spring&scheduler.cron={{skanmothelse.helse.schedule}}")
				.routeId("read_encrypted_PGP_helse_zip_from_sftp")
				.log(LoggingLevel.INFO, log, "Skanmothelse-pgp starter behandling av fil=${file:absolute.path}.")
				.setProperty(PROPERTY_FORSENDELSE_ZIPNAME, simple("${file:name}"))
				.process(exchange -> exchange.setProperty(PROPERTY_FORSENDELSE_BATCHNAVN, cleanDotPgpExtension(simple("${file:name.noext.single}"), exchange)))
				.process(new MdcSetterProcessor())
				.bean(pgpDecryptService)
				.split(new ZipSplitter()).streaming()
					.aggregate(simple("${file:name.noext.single}"), new PostboksHelseSkanningAggregator())
						.completionSize(FORVENTET_ANTALL_PER_FORSENDELSE)
						.completionTimeout(skanmothelseProperties.getHelse().getCompletiontimeout().toMillis())
						.setProperty(PROPERTY_FORSENDELSE_FILEBASENAME, simple("${exchangeProperty.CamelAggregatedCorrelationKey}"))
						.process(new MdcSetterProcessor())
						.process(exchange -> DokCounter.incrementCounter("antall_innkommende", List.of(DOMAIN, HELSE)))
						.process(exchange -> exchange.getIn().getBody(PostboksHelseEnvelope.class).validate())
						.bean(new SkanningmetadataUnmarshaller())
						.bean(new SkanningmetadataCounter())
						.setProperty(PROPERTY_FORSENDELSE_BATCHNAVN, simple("${body.skanningmetadata.journalpost.batchnavn}"))
						.to(PROCESS_PGP_ENCRYPTED)
					.end() // aggregate
				.end() // split
				.process(new MdcRemoverProcessor())
				.log(LoggingLevel.INFO, log, "Skanmothelse-pgp behandlet ferdig fil=${file:absolute.path}.");

		from(PROCESS_PGP_ENCRYPTED)
				.routeId(PROCESS_PGP_ENCRYPTED)
				.process(new MdcSetterProcessor())
				.log(LoggingLevel.INFO, log, "Skanmothelse behandler " + KEY_LOGGING_INFO + ".")
				.bean(postboksHelseService)
				.log(LoggingLevel.INFO, log, "Skanmothelse journalførte journalpostId=${body}. " + KEY_LOGGING_INFO + ".")
				.process(exchange -> DokCounter.incrementCounter("antall_vellykkede", List.of(DOMAIN, HELSE)))
				.process(new MdcRemoverProcessor());

		from(PGP_AVVIK)
				.routeId("pgp_encrypted_avvik_helse")
				.choice().when(body().isInstanceOf(PostboksHelseEnvelope.class))
				.setBody(simple("${body.createZip}"))
				.to("{{skanmothelse.helse.endpointuri}}/{{skanmothelse.helse.filomraade.feilmappe}}" +
						"?{{skanmothelse.helse.endpointconfig}}")
				.otherwise()
				.log(ERROR, log, "Skanmothelse teknisk feil der " + KEY_LOGGING_INFO + ". ikke ble flyttet til feilområde. Må analyseres.")
				.end()
				.process(new MdcRemoverProcessor());

		// @formatter:on
	}

	// Input blir .zip siden .pgp er strippet bort
	private String cleanDotPgpExtension(ValueBuilder value1, Exchange exchange) {
		String stringRepresentation = value1.evaluate(exchange, String.class);
		if (stringRepresentation.contains(".zip")) {
			return stringRepresentation.replace(".zip", "");
		}
		return stringRepresentation;
	}
}
