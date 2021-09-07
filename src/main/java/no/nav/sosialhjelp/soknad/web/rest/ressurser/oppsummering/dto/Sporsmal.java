package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

import java.util.List;

public class Sporsmal {
    private final String tittel;
    private final List<Felt> felt;

    public Sporsmal(String tittel, List<Felt> felt) {
        this.tittel = tittel;
        this.felt = felt;
    }

    public Sporsmal(Builder builder) {
        this.tittel = builder.tittel;
        this.felt = builder.felt;
    }

    public String getTittel() {
        return tittel;
    }

    public List<Felt> getFelt() {
        return felt;
    }

    public static class Builder {
        private String tittel;
        private List<Felt> felt;

        public Builder() {}

        public Builder withTittel(String tittel) {
            this.tittel = tittel;
            return this;
        }

        public Builder withFelt(List<Felt> felt) {
            this.felt = felt;
            return this;
        }

        public Sporsmal build() {
            return new Sporsmal(this);
        }
    }
}
