package no.nav.sosialhjelp.soknad.consumer.person.domain;

public class Matrikkeladresse extends StrukturertAdresse{

    private String kommunenummer;
    private String gardsnummer;
    private String bruksnummer;
    private String festenummer;
    private String seksjonsnummer;
    private String undernummer;
    private String eiendomsnavn;

    public String getKommunenummer() {
        return kommunenummer;
    }

    public Matrikkeladresse withKommunenummer(String kommunenummer) {
        this.kommunenummer = kommunenummer;
        return this;
    }

    public String getGardsnummer() {
        return gardsnummer;
    }

    public Matrikkeladresse withGardsnummer(String gardsnummer) {
        this.gardsnummer = gardsnummer;
        return this;
    }

    public String getBruksnummer() {
        return bruksnummer;
    }

    public Matrikkeladresse withBruksnummer(String bruksnummer) {
        this.bruksnummer = bruksnummer;
        return this;
    }

    public String getFestenummer() {
        return festenummer;
    }

    public Matrikkeladresse withFestenummer(String festenummer) {
        this.festenummer = festenummer;
        return this;
    }

    public String getSeksjonsnummer() {
        return seksjonsnummer;
    }

    public Matrikkeladresse withSeksjonsnummer(String seksjonsnummer) {
        this.seksjonsnummer = seksjonsnummer;
        return this;
    }

    public String getUndernummer() {
        return undernummer;
    }

    public Matrikkeladresse withUndernummer(String undernummer) {
        this.undernummer = undernummer;
        return this;
    }

    public String getEiendomsnavn() {
        return eiendomsnavn;
    }

    public Matrikkeladresse withEiendomsnavn(String eiendomsnavn) {
        this.eiendomsnavn = eiendomsnavn;
        return this;
    }
}
