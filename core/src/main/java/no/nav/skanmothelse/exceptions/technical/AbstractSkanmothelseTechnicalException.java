package no.nav.skanmothelse.exceptions.technical;

public class AbstractSkanmothelseTechnicalException extends RuntimeException {

    public AbstractSkanmothelseTechnicalException(String message) {
        super(message);
    }

    public AbstractSkanmothelseTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
