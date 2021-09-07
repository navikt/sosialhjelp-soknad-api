package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

import java.util.List;

public class Avsnitt {

    private final String tittel;
    private final List<Sporsmal> sporsmal;

    public Avsnitt(
            String tittel,
            List<Sporsmal> sporsmal
    ) {
        this.tittel = tittel;
        this.sporsmal = sporsmal;
    }

    public Avsnitt(Builder builder) {
        this.tittel = builder.tittel;
        this.sporsmal = builder.sporsmal;
    }

    public String getTittel() {
        return tittel;
    }

    public List<Sporsmal> getSporsmal() {
        return sporsmal;
    }

    public static class Builder {
        private String tittel;
        private List<Sporsmal> sporsmal;

        public Builder(){}

        public Builder withTittel(String tittel) {
            this.tittel = tittel;
            return this;
        }

        public Builder withSporsmal(List<Sporsmal> sporsmal) {
            this.sporsmal = sporsmal;
            return this;
        }

        public Avsnitt build() {
            return new Avsnitt(this);
        }
    }
}
