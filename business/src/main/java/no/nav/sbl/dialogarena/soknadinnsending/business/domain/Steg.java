package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

public class Steg {
    public final static Steg VEILEDNING = new Steg("informasjon", "veiledning");
    public final static Steg SOKNAD = new Steg("soknad", "skjema");
    public final static Steg VEDLEGG = new Steg("vedlegg", "vedlegg");
    public final static Steg OPPSUMMERING = new Steg("oppsummering", "sendInn");

    public final String url;
    public final String cmstekst;

    private Steg(String url, String cmstekst) {
        this.url = url;
        this.cmstekst = cmstekst;
    }

}
