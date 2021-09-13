package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

import java.util.List;

public class Steg {

    private final int stegNr;
    private final String tittel;
    private final List<Avsnitt> avsnitt;
<<<<<<< HEAD
<<<<<<< HEAD
=======
    private final boolean erFerdigUtfylt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)

    public Steg(
            int stegNr,
            String tittel,
<<<<<<< HEAD
<<<<<<< HEAD
            List<Avsnitt> avsnitt
=======
            List<Avsnitt> avsnitt,
            boolean erFerdigUtfylt
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
            List<Avsnitt> avsnitt
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
    ) {
        this.stegNr = stegNr;
        this.tittel = tittel;
        this.avsnitt = avsnitt;
<<<<<<< HEAD
<<<<<<< HEAD
=======
        this.erFerdigUtfylt = erFerdigUtfylt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
    }

    public Steg(Builder builder) {
        this.stegNr = builder.stegNr;
        this.tittel = builder.tittel;
        this.avsnitt = builder.avsnitt;
<<<<<<< HEAD
<<<<<<< HEAD
=======
        this.erFerdigUtfylt = builder.erFerdigUtfylt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
    }

    public int getStegNr() {
        return stegNr;
    }

    public String getTittel() {
        return tittel;
    }

<<<<<<< HEAD
<<<<<<< HEAD
    public List<Avsnitt> getAvsnitt() {
        return avsnitt;
    }

=======
    public List<Avsnitt> getBolker() {
=======
    public List<Avsnitt> getAvsnitt() {
>>>>>>> 7ba1c479ec (test PersonopplysningerSteg)
        return avsnitt;
    }

<<<<<<< HEAD
    public boolean getErFerdigUtfylt() {
        return erFerdigUtfylt;
    }

>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
    public static class Builder {
        private int stegNr;
        private String tittel;
        private List<Avsnitt> avsnitt;
<<<<<<< HEAD
<<<<<<< HEAD
=======
        private boolean erFerdigUtfylt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)

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

<<<<<<< HEAD
<<<<<<< HEAD
=======
        public Builder withErFerdigUtfylt(boolean erFerdigUtfylt) {
            this.erFerdigUtfylt = erFerdigUtfylt;
            return this;
        }

>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
=======
>>>>>>> d2f2735ecc (`erUtfylt` pr sporsmal, ikke pr steg)
        public Steg build() {
            return new Steg(this);
        }
    }
}
