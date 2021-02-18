package no.nav.sosialhjelp.soknad.domain.model.exception;

public class SamlUnauthorizedException extends SosialhjelpSoknadApiException {
    public SamlUnauthorizedException(String message) {
        super(message);
    }
}
