package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

public class Steg {
    public static final Steg VEILEDNING = new Steg("informasjonsside", "veiledning");
    public static final Steg SOKNAD = new Steg("soknad", "skjema");
    public static final Steg VEDLEGG = new Steg("vedlegg", "vedlegg");
    public static final Steg OPPSUMMERING = new Steg("oppsummering", "sendInn");

    public final String url;
    public final String cmstekst;

    private Steg(String url, String cmstekst) {
        this.url = url;
        this.cmstekst = cmstekst;
    }

}
