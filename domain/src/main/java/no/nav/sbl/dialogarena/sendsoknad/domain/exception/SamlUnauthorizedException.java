package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

public class SamlUnauthorizedException extends SosialhjelpSoknadApiException {
    public SamlUnauthorizedException(String message) {
        super(message);
    }
}
