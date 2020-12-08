package no.nav.sbl.dialogarena.sendsoknad.domain;


public class Matrikkeladresse {

    private final String matrikkelId;
    private final String postnummer;
    private final String tilleggsnavn;
    private final String kommunenummer;
    private final String bruksenhetsnummer;

//    TODO: disse feltene skal settes i JsonMatrikkeladresse: "kommunenummer", "gaardsnummer", "bruksnummer", "festenummer", "seksjonsnummer", "undernummer"

    public Matrikkeladresse(
            String matrikkelId,
            String postnummer,
            String tilleggsnavn,
            String kommunenummer,
            String bruksenhetsnummer) {
        this.matrikkelId = matrikkelId;
        this.postnummer = postnummer;
        this.tilleggsnavn = tilleggsnavn;
        this.kommunenummer = kommunenummer;
        this.bruksenhetsnummer = bruksenhetsnummer;
    }

    public String getMatrikkelId() {
        return matrikkelId;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getTilleggsnavn() {
        return tilleggsnavn;
    }

    public String getKommunenummer() {
        return kommunenummer;
    }

    public String getBruksenhetsnummer() {
        return bruksenhetsnummer;
    }

}
