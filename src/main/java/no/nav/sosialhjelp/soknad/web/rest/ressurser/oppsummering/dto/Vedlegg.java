package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

public class Vedlegg {

    private final String filnavn;
    private final String lenke;

    public Vedlegg(String filnavn, String lenke) {
        this.filnavn = filnavn;
        this.lenke = lenke;
    }

    public String getFilnavn() {
        return filnavn;
    }

    public String getLenke() {
        return lenke;
    }
}
