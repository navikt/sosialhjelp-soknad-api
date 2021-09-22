package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

public class Vedlegg {

    private final String filnavn;
    private final String lenke;

    public Vedlegg(String filnavn, String lenke) {
        this.filnavn = filnavn;
        this.lenke = lenke;
    }

    public Vedlegg(Builder builder) {
        this.filnavn = builder.filnavn;
        this.lenke = builder.lenke;
    }

    public String getFilnavn() {
        return filnavn;
    }

    public String getLenke() {
        return lenke;
    }

    public static class Builder {
        private String filnavn;
        private String lenke;

        public Builder(){}

        public Builder withFilnavn(String filnavn) {
            this.filnavn = filnavn;
            return this;
        }

        public Builder withLenke(String lenke) {
            this.lenke = lenke;
            return this;
        }

        public Vedlegg build() {
            return new Vedlegg(this);
        }
    }
}
