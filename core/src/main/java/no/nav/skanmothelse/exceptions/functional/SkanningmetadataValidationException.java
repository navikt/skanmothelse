package no.nav.skanmothelse.exceptions.functional;

/**
 * Brukes når en skanningmetadata xml ikke validerer.
 */
public class SkanningmetadataValidationException extends AbstractSkanmothelseFunctionalException {
    public SkanningmetadataValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
