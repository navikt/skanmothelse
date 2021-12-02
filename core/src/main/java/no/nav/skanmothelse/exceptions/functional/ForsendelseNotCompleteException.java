package no.nav.skanmothelse.exceptions.functional;

/**
 * Brukes når en inngående forsendelse ikke er komplett.
 * Eks mangler zip, xml eller ocr.
 */
public class ForsendelseNotCompleteException extends AbstractSkanmothelseFunctionalException {
    public ForsendelseNotCompleteException(String message) {
        super(message);
    }
}
