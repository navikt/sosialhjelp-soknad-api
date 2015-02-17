package no.nav.sbl.dialogarena.rest.feil;

public class Feilmelding {

    //TODO: burde dette være noe annet enn cms-nøkkel?
    // TODO: Caller ID
    private String id;
    private String message;

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
