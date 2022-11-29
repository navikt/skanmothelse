package no.nav.skanmothelse.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.skanmothelse.exceptions.functional.AbstractSkanmothelseFunctionalException;
import org.bouncycastle.openpgp.PGPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DokCounter {
    private static final String DOK_SKANMOTHELSE = "dok_skanmothelse_";
    private static final String TOTAL = "_total";
    private static final String EXCEPTION = "exception";
    private static final String ERROR_TYPE = "error_type";
    private static final String EXCEPTION_NAME = "exception_name";
    private static final String FUNCTIONAL_ERROR = "functional";
    private static final String TECHNICAL_ERROR = "technical";
    public static final String DOMAIN = "domain";
    public static final String CORE = "core";
    public static final String HELSE = "helse";

    private static MeterRegistry meterRegistry;

    @Autowired
    public DokCounter(MeterRegistry meterRegistry){
        DokCounter.meterRegistry = meterRegistry;
    }

    public static void incrementCounter(Map<String, String> metadata){
        metadata.forEach(DokCounter::incrementCounter);
    }

    public static void incrementCounter(String key, List<String> tags) {
        Counter.builder(DOK_SKANMOTHELSE + key + TOTAL)
                .tags(tags.toArray(new String[0]))
                .register(meterRegistry)
                .increment();
    }


    private static void incrementCounter(String key, String value) {
        Counter.builder(DOK_SKANMOTHELSE + key + TOTAL)
                .tags(key, value)
                .register(meterRegistry)
                .increment();
    }

    public static void incrementError(Throwable throwable, String domain){
        Counter.builder(DOK_SKANMOTHELSE + EXCEPTION)
                .tags(ERROR_TYPE, isFunctionalException(throwable) ? FUNCTIONAL_ERROR : TECHNICAL_ERROR)
                .tags(EXCEPTION_NAME, throwable.getClass().getSimpleName())
                .tag(DOMAIN, isEmptyString(domain) ? CORE : domain)
                .register(meterRegistry)
                .increment();
    }

    private static boolean isFunctionalException(Throwable e) {
        return e instanceof AbstractSkanmothelseFunctionalException
                || e instanceof PGPException; // Feil for PGP-kryptering
    }

    private static boolean isEmptyString(String string) {
        return string == null || string.isBlank();
    }
}
