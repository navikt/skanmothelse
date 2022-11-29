package no.nav.skanmothelse.helse;

import lombok.extern.slf4j.Slf4j;
import no.nav.skanmothelse.config.properties.SkanmothelseProperties;
import no.nav.skanmothelse.exceptions.functional.AbstractSkanmothelseFunctionalException;
import no.nav.skanmothelse.metrics.DokCounter;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static no.nav.skanmothelse.metrics.DokCounter.DOMAIN;
import static no.nav.skanmothelse.metrics.DokCounter.HELSE;
import static org.apache.camel.Exchange.FILE_NAME;
import static org.apache.camel.Exchange.FILE_NAME_PRODUCED;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;


@Slf4j
@Component
public class PostboksHelseRoute extends RouteBuilder {
	public static final String PROPERTY_FORSENDELSE_ZIPNAME = "ForsendelseZipname";
	public static final String PROPERTY_FORSENDELSE_BATCHNAVN = "ForsendelseBatchNavn";
	public static final String PROPERTY_FORSENDELSE_FILEBASENAME = "ForsendelseFileBasename";
	public static final String KEY_LOGGING_INFO = "fil=${exchangeProperty." + PROPERTY_FORSENDELSE_FILEBASENAME + "}, batch=${exchangeProperty." + PROPERTY_FORSENDELSE_BATCHNAVN + "}";
	static final int FORVENTET_ANTALL_PER_FORSENDELSE = 3;

	private final SkanmothelseProperties skanmothelseProperties;
	private final PostboksHelseService postboksHelseService;

	@Autowired
	public PostboksHelseRoute(PostboksHelseService postboksHelseService, SkanmothelseProperties skanmothelseProperties) {
		this.postboksHelseService = postboksHelseService;
		this.skanmothelseProperties = skanmothelseProperties;
	}

	@Override
	public void configure() {

		// @formatter:off
        onException(Exception.class)
                .handled(true)
                .process(new MdcSetterProcessor())
                .process(new ErrorMetricsProcessor())
                .log(ERROR, log, "Skanmothelse feilet teknisk for " + KEY_LOGGING_INFO + ". ${exception}")
                .setHeader(FILE_NAME, simple("${exchangeProperty." + PROPERTY_FORSENDELSE_BATCHNAVN + "}/${exchangeProperty." + PROPERTY_FORSENDELSE_FILEBASENAME + "}-teknisk.zip"))
                .to("direct:avvik")
                .log(ERROR, log, "Skanmothelse skrev feiletzip=${header." + FILE_NAME_PRODUCED + "} til feilmappe. " + KEY_LOGGING_INFO + ".");

        // Kjente funksjonelle feil
        onException(AbstractSkanmothelseFunctionalException.class)
                .handled(true)
                .process(new MdcSetterProcessor())
                .process(new ErrorMetricsProcessor())
                .log(WARN, log, "Skanmothelse feilet funksjonelt for " + KEY_LOGGING_INFO + ". ${exception}")
                .setHeader(FILE_NAME, simple("${exchangeProperty." + PROPERTY_FORSENDELSE_BATCHNAVN + "}/${exchangeProperty." + PROPERTY_FORSENDELSE_FILEBASENAME + "}.zip"))
                .to("direct:avvik")
                .log(WARN, log, "Skanmothelse skrev feiletzip=${header." + FILE_NAME_PRODUCED + "} til feilmappe. " + KEY_LOGGING_INFO + ".");

        from("{{skanmothelse.helse.endpointuri}}/{{skanmothelse.helse.filomraade.inngaaendemappe}}" +
                "?{{skanmothelse.helse.endpointconfig}}" +
                "&delay=" + TimeUnit.SECONDS.toMillis(60) +
                "&antExclude=*zip.pgp, *ZIP.pgp" +
                "&antInclude=*.zip,*.ZIP" +
                "&initialDelay=1000" +
                "&maxMessagesPerPoll=10" +
                "&move=processed" +
                "&jailStartingDirectory=false"+
                "&scheduler=spring&scheduler.cron={{skanmothelse.helse.schedule}}")
                .routeId("read_zip_from_sftp")
                .log(INFO, log, "Skanmothelse starter behandling av fil=${file:absolute.path}.")
                .setProperty(PROPERTY_FORSENDELSE_ZIPNAME, simple("${file:name}"))
                .setProperty(PROPERTY_FORSENDELSE_BATCHNAVN, simple("${file:name.noext.single}"))
                .process(new MdcSetterProcessor())
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
						.to("direct:process_helse")
					.end() // aggregate
                .end() // split
                .process(new MdcRemoverProcessor())
                .log(INFO, log, "Skanmothelse behandlet ferdig fil=${file:absolute.path}.");

        from("direct:process_helse")
                .routeId("process_helse")
                .process(new MdcSetterProcessor())
                .log(INFO, log, "Skanmothelse behandler " + KEY_LOGGING_INFO + ".")
                .bean(postboksHelseService)
                .log(INFO, log, "Skanmothelse journalførte journalpostId=${body}. " + KEY_LOGGING_INFO + ".")
                .process(exchange -> DokCounter.incrementCounter("antall_vellykkede", List.of(DOMAIN, HELSE)))
                .process(new MdcRemoverProcessor());

        from("direct:avvik")
                .routeId("avvik")
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
}
