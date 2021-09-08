package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

import java.util.List;

public class Sporsmal {
    private final String tittel;
    private final List<Felt> felt;
<<<<<<< HEAD
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
=======
    private final boolean erUtfylt;
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)

    public Sporsmal(
            String tittel,
            List<Felt> felt,
            boolean erUtfylt
    ) {
        this.tittel = tittel;
        this.felt = felt;
<<<<<<< HEAD
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
        this.erUtfylt = erUtfylt;
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
    }

    public Sporsmal(Builder builder) {
        this.tittel = builder.tittel;
        this.felt = builder.felt;
<<<<<<< HEAD
<<<<<<< HEAD
        this.erUtfylt = builder.erUtfylt;
=======
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
        this.erUtfylt = builder.erUtfylt;
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
    }

    public String getTittel() {
        return tittel;
    }

    public List<Felt> getFelt() {
        return felt;
    }

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
    public boolean getErUtfylt() {
        return erUtfylt;
    }

    public static class Builder {
        private String tittel;
        private List<Felt> felt;
        private boolean erUtfylt;
<<<<<<< HEAD
=======
    public static class Builder {
        private String tittel;
        private List<Felt> felt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)

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
<<<<<<< HEAD
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
        public Builder withErUtfylt(boolean erUtfylt) {
            this.erUtfylt = erUtfylt;
            return this;
        }

<<<<<<< HEAD
=======
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
        public Sporsmal build() {
            return new Sporsmal(this);
        }
    }
}
