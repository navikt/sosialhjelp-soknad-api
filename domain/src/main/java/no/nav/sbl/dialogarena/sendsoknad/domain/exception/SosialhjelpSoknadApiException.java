package no.nav.sbl.dialogarena.sendsoknad.domain.exception;

public class SosialhjelpSoknadApiException extends RuntimeException {

    private String id;

    public SosialhjelpSoknadApiException(String melding) {
        super(melding);
    }

    public SosialhjelpSoknadApiException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public SosialhjelpSoknadApiException(String message, Throwable cause, String id) {
        super(message, cause);
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
