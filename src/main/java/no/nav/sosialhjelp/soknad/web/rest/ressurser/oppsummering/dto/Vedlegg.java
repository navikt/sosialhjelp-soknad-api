package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

public class Vedlegg {

    private final String filnavn;
    private final String uuid;

    public Vedlegg(String filnavn, String uuid) {
        this.filnavn = filnavn;
        this.uuid = uuid;
    }

    public Vedlegg(Builder builder) {
        this.filnavn = builder.filnavn;
        this.uuid = builder.uuid;
    }

    public String getFilnavn() {
        return filnavn;
    }

    public String getUuid() {
        return uuid;
    }

    public static class Builder {
        private String filnavn;
        private String uuid;

        public Builder(){}

        public Builder withFilnavn(String filnavn) {
            this.filnavn = filnavn;
            return this;
        }

        public Builder withUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Vedlegg build() {
            return new Vedlegg(this);
        }
    }
}
