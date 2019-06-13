package no.nav.sbl.dialogarena.sendsoknad.domain;

public class AdresserOgKontonummer {
    private Adresse gjeldendeAdresse;
    private Adresse sekundarAdresse;
    private Adresse folkeregistrertAdresse;
    private boolean utenlandskAdresse;
    private boolean bosattIEOSLand;
    private String kontonummer;
    private boolean utenlandskBankkonto;
    private String utenlandskKontoBanknavn;
    private String utenlandskKontoLand;
    private Adresse midlertidigAdresse;

    public Adresse getGjeldendeAdresse() {
        return gjeldendeAdresse;
    }
    public Adresse getMidlertidigAdresse() { return midlertidigAdresse; }

    public Adresse getSekundarAdresse() {
        return sekundarAdresse;
    }

    public Adresse getFolkeregistrertAdresse() {
        return folkeregistrertAdresse;
    }

    public boolean isUtenlandskAdresse() {
        return utenlandskAdresse;
    }

    public boolean isBosattIEOSLand() {
        return bosattIEOSLand;
    }

    public String getKontonummer() {
        return kontonummer;
    }

    public boolean isUtenlandskBankkonto() {
        return utenlandskBankkonto;
    }

    public String getUtenlandskKontoBanknavn() {
        return utenlandskKontoBanknavn;
    }

    public String getUtenlandskKontoLand() {
        return utenlandskKontoLand;
    }



    public AdresserOgKontonummer withGjeldendeAdresse(Adresse gjeldendeAdresse) {
        this.gjeldendeAdresse = gjeldendeAdresse;
        return this;
    }

    public AdresserOgKontonummer withSekundarAdresse(Adresse sekundarAdresse) {
        this.sekundarAdresse = sekundarAdresse;
        return this;
    }

    public AdresserOgKontonummer withFolkeregistrertAdresse(Adresse folkeregistrertAdresse) {
        this.folkeregistrertAdresse = folkeregistrertAdresse;
        return this;
    }

    public AdresserOgKontonummer withUtenlandskAdresse(boolean utenlandskAdresse) {
        this.utenlandskAdresse = utenlandskAdresse;
        return this;
    }

    public AdresserOgKontonummer withBosattIEOSLand(boolean bosattIEOSLand) {
        this.bosattIEOSLand = bosattIEOSLand;
        return this;
    }

    public AdresserOgKontonummer withKontonummer(String kontonummer) {
        this.kontonummer = kontonummer;
        return this;
    }

    public AdresserOgKontonummer withUtenlandskBankkonto(boolean utenlandskBankkonto) {
        this.utenlandskBankkonto = utenlandskBankkonto;
        return this;
    }

    public AdresserOgKontonummer withUtenlandskKontoBanknavn(String utenlandskKontoBanknavn) {
        this.utenlandskKontoBanknavn = utenlandskKontoBanknavn;
        return this;
    }

    public AdresserOgKontonummer withUtenlandskKontoLand(String utenlandskKontoLand) {
        this.utenlandskKontoLand = utenlandskKontoLand;
        return this;
    }

    public AdresserOgKontonummer withMidlertidigAdresse(Adresse midlertidigAdresse) {
        this.midlertidigAdresse = midlertidigAdresse;
        return this;
    }
}