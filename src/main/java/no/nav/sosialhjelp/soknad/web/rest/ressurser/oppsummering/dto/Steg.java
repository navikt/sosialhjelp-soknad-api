package no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto;

import java.util.List;

public class Steg {

    private final int stegNr;
    private final String tittel;
    private final List<Avsnitt> avsnitt;
<<<<<<< HEAD
=======
    private final boolean erFerdigUtfylt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)

    public Steg(
            int stegNr,
            String tittel,
<<<<<<< HEAD
            List<Avsnitt> avsnitt
=======
            List<Avsnitt> avsnitt,
            boolean erFerdigUtfylt
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
    ) {
        this.stegNr = stegNr;
        this.tittel = tittel;
        this.avsnitt = avsnitt;
<<<<<<< HEAD
=======
        this.erFerdigUtfylt = erFerdigUtfylt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
    }

    public Steg(Builder builder) {
        this.stegNr = builder.stegNr;
        this.tittel = builder.tittel;
        this.avsnitt = builder.avsnitt;
<<<<<<< HEAD
=======
        this.erFerdigUtfylt = builder.erFerdigUtfylt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
    }

    public int getStegNr() {
        return stegNr;
    }

    public String getTittel() {
        return tittel;
    }

<<<<<<< HEAD
    public List<Avsnitt> getAvsnitt() {
        return avsnitt;
    }

=======
    public List<Avsnitt> getBolker() {
        return avsnitt;
    }

    public boolean getErFerdigUtfylt() {
        return erFerdigUtfylt;
    }

>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
    public static class Builder {
        private int stegNr;
        private String tittel;
        private List<Avsnitt> avsnitt;
<<<<<<< HEAD
=======
        private boolean erFerdigUtfylt;
>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)

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
=======
        public Builder withErFerdigUtfylt(boolean erFerdigUtfylt) {
            this.erFerdigUtfylt = erFerdigUtfylt;
            return this;
        }

>>>>>>> 51bfd24483 (utkast endepunkt til ny oppsummering-side. wip)
        public Steg build() {
            return new Steg(this);
        }
    }
}
