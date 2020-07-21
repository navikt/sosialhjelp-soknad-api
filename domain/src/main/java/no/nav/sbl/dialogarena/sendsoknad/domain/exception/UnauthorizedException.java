package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

public class UnauthorizedException extends SosialhjelpSoknadApiException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
