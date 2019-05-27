package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

public class AuthorizationException extends ModigException {
    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
