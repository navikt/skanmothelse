package no.nav.skanmothelse.exceptions.technical;

public class AbstractSkanmotovrigTechnicalException extends RuntimeException {

    public AbstractSkanmotovrigTechnicalException(String message) {
        super(message);
    }

    public AbstractSkanmotovrigTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
