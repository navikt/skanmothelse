package no.nav.skanmothelse.exceptions.functional;

public class AbstractSkanmothelseFunctionalException extends RuntimeException {

    public AbstractSkanmothelseFunctionalException(String message) {
        super(message);
    }

    public AbstractSkanmothelseFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
