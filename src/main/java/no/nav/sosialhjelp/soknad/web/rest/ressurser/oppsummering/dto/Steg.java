package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

import java.util.List;

public class Steg {

    private final int stegNr;
    private final String tittel;
    private final List<Avsnitt> avsnitt;

    public Steg(
            int stegNr,
            String tittel,
            List<Avsnitt> avsnitt
    ) {
        this.stegNr = stegNr;
        this.tittel = tittel;
        this.avsnitt = avsnitt;
    }

    public Steg(Builder builder) {
        this.stegNr = builder.stegNr;
        this.tittel = builder.tittel;
        this.avsnitt = builder.avsnitt;
    }

    public int getStegNr() {
        return stegNr;
    }

    public String getTittel() {
        return tittel;
    }

    public List<Avsnitt> getAvsnitt() {
        return avsnitt;
    }

    public static class Builder {
        private int stegNr;
        private String tittel;
        private List<Avsnitt> avsnitt;

        public Builder(){}

        public Builder withStegNr(int stegNr) {
            this.stegNr = stegNr;
            return this;
        }

        public Builder withTittel(String tittel) {
            this.tittel = tittel;
            return this;
        }

        public Builder withAvsnitt(List<Avsnitt> avsnitt) {
            this.avsnitt = avsnitt;
            return this;
        }

        public Steg build() {
            return new Steg(this);
        }
    }
}
