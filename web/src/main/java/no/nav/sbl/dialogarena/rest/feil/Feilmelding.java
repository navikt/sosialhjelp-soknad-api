package no.nav.sbl.dialogarena.rest.feil;

public class Feilmelding {

    public static final String NO_BIGIP_5XX_REDIRECT = "X-Escape-5xx-Redirect";

    private String id;
    private String message;

    @SuppressWarnings("unused")
    public Feilmelding() {}
    public Feilmelding(String id, String message) {
        this.id = id;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
