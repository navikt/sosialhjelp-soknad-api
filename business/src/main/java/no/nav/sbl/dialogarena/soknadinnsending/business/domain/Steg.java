package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

public class Steg {
    public static Steg VEILEDNING = new Steg("informasjon", "veiledning");
    public static Steg SOKNAD = new Steg("soknad", "skjema");
    public static Steg VEDLEGG = new Steg("vedlegg", "vedlegg");
    public static Steg OPPSUMMERING = new Steg("oppsummering", "sendInn");

    public final String url;
    public final String cmstekst;

    private Steg(String url, String cmstekst) {
        this.url = url;
        this.cmstekst = cmstekst;
    }

}
