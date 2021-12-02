package no.nav.skanmothelse.helse;

import no.nav.skanmothelse.metrics.DokCounter;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class ErrorMetricsProcessor implements Processor {
    private final String CAMEL_EXCEPTION_CAUGHT = "CamelExceptionCaught";

    @Override
    public void process(Exchange exchange) throws Exception {
        Object exception = exchange.getProperty(CAMEL_EXCEPTION_CAUGHT);
        if(exception instanceof Throwable){
            DokCounter.incrementError((Throwable) exception, DokCounter.HELSE);
        }
    }
}
