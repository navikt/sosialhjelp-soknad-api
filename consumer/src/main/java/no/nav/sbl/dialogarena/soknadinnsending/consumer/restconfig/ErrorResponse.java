package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

public class ErrorResponse {

    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
