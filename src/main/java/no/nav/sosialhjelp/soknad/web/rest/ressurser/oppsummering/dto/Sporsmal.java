package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

import java.util.List;

public class Sporsmal {
    private final String tittel;
    private final List<Felt> felt;
    private final boolean erUtfylt;

    public Sporsmal(
            String tittel,
            List<Felt> felt,
            boolean erUtfylt
    ) {
        this.tittel = tittel;
        this.felt = felt;
        this.erUtfylt = erUtfylt;
    }

    public Sporsmal(Builder builder) {
        this.tittel = builder.tittel;
        this.felt = builder.felt;
        this.erUtfylt = builder.erUtfylt;
    }

    public String getTittel() {
        return tittel;
    }

    public List<Felt> getFelt() {
        return felt;
    }

    public boolean getErUtfylt() {
        return erUtfylt;
    }

    public static class Builder {
        private String tittel;
        private List<Felt> felt;
        private boolean erUtfylt;

        public Builder() {
        }

        public Builder withTittel(String tittel) {
            this.tittel = tittel;
            return this;
        }

        public Builder withFelt(List<Felt> felt) {
            this.felt = felt;
            return this;
        }

        public Builder withErUtfylt(boolean erUtfylt) {
            this.erUtfylt = erUtfylt;
            return this;
        }

        public Sporsmal build() {
            return new Sporsmal(this);
        }
    }
}
