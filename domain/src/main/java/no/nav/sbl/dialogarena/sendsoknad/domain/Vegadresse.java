package no.nav.sbl.dialogarena.sendsoknad.domain;

public class Vegadresse {

    private final String adressenavn;
    private final Integer husnummer;
    private final String husbokstav;
    private final String tilleggsnavn;
    private final String postnummer;
    private final String kommunenummer;
    private final String bruksenhetsnummer;

    public Vegadresse(
            String adressenavn,
            Integer husnummer,
            String husbokstav,
            String tilleggsnavn,
            String postnummer,
            String kommunenummer,
            String bruksenhetsnummer
    ) {
        this.adressenavn = adressenavn;
        this.husnummer = husnummer;
        this.husbokstav = husbokstav;
        this.tilleggsnavn = tilleggsnavn;
        this.postnummer = postnummer;
        this.kommunenummer = kommunenummer;
        this.bruksenhetsnummer = bruksenhetsnummer;
    }

    public String getAdressenavn() {
        return adressenavn;
    }

    public Integer getHusnummer() {
        return husnummer;
    }

    public String getHusbokstav() {
        return husbokstav;
    }

    public String getTilleggsnavn() {
        return tilleggsnavn;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getKommunenummer() {
        return kommunenummer;
    }

    public String getBruksenhetsnummer() {
        return bruksenhetsnummer;
    }
}
