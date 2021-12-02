package no.nav.skanmothelse.exceptions.functional;

public class SkanmothelseFunctionalException extends AbstractSkanmothelseFunctionalException {
    public SkanmothelseFunctionalException(String message) {
        super(message);
    }
    public SkanmothelseFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
