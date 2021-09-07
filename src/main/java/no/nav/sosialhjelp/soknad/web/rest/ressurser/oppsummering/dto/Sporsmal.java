package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

import java.util.List;

public class Sporsmal {
    private final String tittel;
    private final List<Felt> felt;
<<<<<<< HEAD
    private final boolean erUtfylt;

    public Sporsmal(
            String tittel,
            List<Felt> felt,
            boolean erUtfylt
    ) {
        this.tittel = tittel;
        this.felt = felt;
        this.erUtfylt = erUtfylt;
=======

    public Sporsmal(String tittel, List<Felt> felt) {
        this.tittel = tittel;
        this.felt = felt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
    }

    public Sporsmal(Builder builder) {
        this.tittel = builder.tittel;
        this.felt = builder.felt;
<<<<<<< HEAD
        this.erUtfylt = builder.erUtfylt;
=======
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
    }

    public String getTittel() {
        return tittel;
    }

    public List<Felt> getFelt() {
        return felt;
    }

<<<<<<< HEAD
    public boolean getErUtfylt() {
        return erUtfylt;
    }

    public static class Builder {
        private String tittel;
        private List<Felt> felt;
        private boolean erUtfylt;
=======
    public static class Builder {
        private String tittel;
        private List<Felt> felt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)

        public Builder() {}

        public Builder withTittel(String tittel) {
            this.tittel = tittel;
            return this;
        }

        public Builder withFelt(List<Felt> felt) {
            this.felt = felt;
            return this;
        }

<<<<<<< HEAD
        public Builder withErUtfylt(boolean erUtfylt) {
            this.erUtfylt = erUtfylt;
            return this;
        }

=======
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
        public Sporsmal build() {
            return new Sporsmal(this);
        }
    }
}
