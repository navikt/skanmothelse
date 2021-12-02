package no.nav.skanmothelse.exceptions.functional;

public class InvalidMetadataException extends AbstractSkanmotovrigFunctionalException {

    public InvalidMetadataException(String message) {
        super(message);
    }

    public InvalidMetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
