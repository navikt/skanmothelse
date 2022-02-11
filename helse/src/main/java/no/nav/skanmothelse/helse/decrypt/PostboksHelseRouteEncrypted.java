package no.nav.skanmothelse.helse.decrypt;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.exception.ZipException;
import no.nav.skanmothelse.config.properties.SkanmothelseProperties;
import no.nav.skanmothelse.decrypt.ZipSplitterEncrypted;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class PostboksHelseRouteEncrypted extends RouteBuilder {
    public static final String PROPERTY_FORSENDELSE_ZIPNAME = "ForsendelseZipname";
    public static final String PROPERTY_FORSENDELSE_BATCHNAVN = "ForsendelseBatchNavn";
    public static final String PROPERTY_FORSENDELSE_FILEBASENAME = "ForsendelseFileBasename";
    public static final String KEY_LOGGING_INFO = "fil=${exchangeProperty." + PROPERTY_FORSENDELSE_FILEBASENAME + "}, batch=${exchangeProperty." + PROPERTY_FORSENDELSE_BATCHNAVN + "}";
    static final int FORVENTET_ANTALL_PER_FORSENDELSE = 3;

    private final SkanmothelseProperties skanmothelseProperties;
    private final PostboksHelseService postboksHelseService;
    private final String passphrase;

    @Autowired
    public PostboksHelseRouteEncrypted(@Value("${skanmothelse.secret.passphrase}") String passphrase,
                                       PostboksHelseService postboksHelseService, SkanmothelseProperties skanmothelseProperties) {
        this.postboksHelseService = postboksHelseService;
        this.skanmothelseProperties = skanmothelseProperties;
        this.passphrase = passphrase;
    }

    @Override
    public void configure() throws Exception {
        onException(Exception.class)
                .handled(true)
                .process(new MdcSetterProcessor())
                .process(new ErrorMetricsProcessor())
                .log(LoggingLevel.ERROR, log, "Skanmothelse feilet teknisk for " + KEY_LOGGING_INFO + ". ${exception}")
                .setHeader(Exchange.FILE_NAME, simple("${exchangeProperty." + PROPERTY_FORSENDELSE_BATCHNAVN + "}/${exchangeProperty." + PROPERTY_FORSENDELSE_FILEBASENAME + "}-teknisk.zip"))
                .to("direct:encrypted_avvik")
                .log(LoggingLevel.ERROR, log, "Skanmothelse skrev feiletzip=${header." + Exchange.FILE_NAME_PRODUCED + "} til feilmappe. " + KEY_LOGGING_INFO + ".");

        onException(ZipException.class)
                .handled(true)
                .process(new MdcSetterProcessor())
                .process(new ErrorMetricsProcessor())
                .log(LoggingLevel.WARN, log, "Feil passord for en fil " + KEY_LOGGING_INFO + ". ${exception}")
                .setHeader(Exchange.FILE_NAME, simple("${exchangeProperty." + PROPERTY_FORSENDELSE_BATCHNAVN + "}${exchangeProperty." + PROPERTY_FORSENDELSE_FILEBASENAME + "}.enc.zip"))
                .to("{{skanmothelse.helse.endpointuri}}/{{skanmothelse.helse.filomraade.feilmappe}}" +
                        "?{{skanmothelse.helse.endpointconfig}}")
                .log(LoggingLevel.WARN, log, "Skanmothelse skrev feiletzip=${header." + Exchange.FILE_NAME_PRODUCED + "} til feilmappe. " + KEY_LOGGING_INFO + ".")
                .end()
                .process(new MdcRemoverProcessor());

        // Kjente funksjonelle feil
        onException(AbstractSkanmothelseFunctionalException.class)
                .handled(true)
                .process(new MdcSetterProcessor())
                .process(new ErrorMetricsProcessor())
                .log(LoggingLevel.WARN, log, "Skanmothelse feilet funksjonelt for " + KEY_LOGGING_INFO + ". ${exception}")
                .setHeader(Exchange.FILE_NAME, simple("${exchangeProperty." + PROPERTY_FORSENDELSE_BATCHNAVN + "}/${exchangeProperty." + PROPERTY_FORSENDELSE_FILEBASENAME + "}.zip"))
                .to("direct:encrypted_avvik")
                .log(LoggingLevel.WARN, log, "Skanmothelse skrev feiletzip=${header." + Exchange.FILE_NAME_PRODUCED + "} til feilmappe. " + KEY_LOGGING_INFO + ".");

        from("{{skanmothelse.helse.endpointuri}}/{{skanmothelse.helse.filomraade.inngaaendemappe}}" +
                "?{{skanmothelse.helse.endpointconfig}}" +
                "&delay=" + TimeUnit.SECONDS.toMillis(60) +
                "&antInclude=*.enc.zip,*.enc.ZIP" +
                "&initialDelay=1000" +
                "&maxMessagesPerPoll=10" +
                "&move=processed" +
                "&scheduler=spring&scheduler.cron={{skanmothelse.helse.schedule}}")
                .routeId("read_encrypted_helse_from_sftp")
                .log(LoggingLevel.INFO, log, "Skanmothelse starter behandling av fil=${file:absolute.path}.")
                .setProperty(PROPERTY_FORSENDELSE_ZIPNAME, simple("${file:name}"))
                .process(exchange -> exchange.setProperty(PROPERTY_FORSENDELSE_BATCHNAVN, cleanDotEncExtension(simple("${file:name.noext.single}"),exchange)))
                .process(new MdcSetterProcessor())
                .split(new ZipSplitterEncrypted(passphrase)).streaming()
                .aggregate(simple("${file:name.noext.single}"), new PostboksHelseSkanningAggregator())
                .completionSize(FORVENTET_ANTALL_PER_FORSENDELSE)
                .completionTimeout(skanmothelseProperties.getHelse().getCompletiontimeout().toMillis())
                .setProperty(PROPERTY_FORSENDELSE_FILEBASENAME, simple("${exchangeProperty.CamelAggregatedCorrelationKey}"))
                .process(new MdcSetterProcessor())
                .process(exchange -> DokCounter.incrementCounter("antall_innkommende", List.of(DokCounter.DOMAIN, DokCounter.HELSE)))
                .process(exchange -> exchange.getIn().getBody(PostboksHelseEnvelope.class).validate())
                .bean(new SkanningmetadataUnmarshaller())
                .bean(new SkanningmetadataCounter())
                .setProperty(PROPERTY_FORSENDELSE_BATCHNAVN, simple("${body.skanningmetadata.journalpost.batchnavn}"))
                .to("direct:encrypted_process_helse")
                .end() // aggregate
                .end() // split
                .process(new MdcRemoverProcessor())
                .log(LoggingLevel.INFO, log, "Skanmothelse behandlet ferdig fil=${file:absolute.path}.");

        from("direct:encrypted_process_helse")
                .routeId("encrypted_process_helse")
                .process(new MdcSetterProcessor())
                .log(LoggingLevel.INFO, log, "Skanmothelse behandler " + KEY_LOGGING_INFO + ".")
                .bean(postboksHelseService)
                .log(LoggingLevel.INFO, log, "Skanmothelse journalførte journalpostId=${body}. " + KEY_LOGGING_INFO + ".")
                .process(exchange -> DokCounter.incrementCounter("antall_vellykkede", List.of(DokCounter.DOMAIN, DokCounter.HELSE)))
                .process(new MdcRemoverProcessor());

        from("direct:encrypted_avvik")
                .routeId("encrypted_avvik")
                .choice().when(body().isInstanceOf(PostboksHelseEnvelope.class))
                .setBody(simple("${body.createZip}"))
                .to("{{skanmothelse.helse.endpointuri}}/{{skanmothelse.helse.filomraade.feilmappe}}" +
                        "?{{skanmothelse.helse.endpointconfig}}")
                .otherwise()
                .log(LoggingLevel.ERROR, log, "Skanmothelse teknisk feil der " + KEY_LOGGING_INFO + ". ikke ble flyttet til feilområde. Må analyseres.")
                .end()
                .process(new MdcRemoverProcessor());
    }

    private String cleanDotEncExtension(ValueBuilder value1, Exchange exchange) {
        String stringRepresentation = value1.evaluate(exchange, String.class);
        if (stringRepresentation.contains(".enc")) {
            return stringRepresentation.replace(".enc", "");
        }
        return stringRepresentation;
    }
}
