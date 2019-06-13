package no.nav.sbl.dialogarena.sendsoknad.domain.personalia;

import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.StatsborgerskapType;
import org.joda.time.LocalDate;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Adressetype.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.LandListe.EOS;

public class Personalia {

    private String fnr;
    private LocalDate fodselsdato;
    private String alder;
    private String navn;
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String epost;
    private String statsborgerskap;
    private String kjonn;
    private Adresse gjeldendeAdresse;
    private Adresse midlertidigAdresse;
    private Adresse sekundarAdresse;
    private Adresse folkeregistrertAdresse;
    private String kontonummer;
    private String diskresjonskode;
    private Boolean erUtenlandskBankkonto;
    private String utenlandskKontoBanknavn;
    private String utenlandskKontoLand;
    private String mobiltelefonnummer;
    private String sivilstatus;
    private Ektefelle ektefelle;
    public Personalia() {
    }

    public String getFnr() {
        return fnr;
    }

    public void setFnr(String fnr) {
        this.fnr = fnr;
    }

    public LocalDate getFodselsdato() {
        return fodselsdato;
    }

    public void setFodselsdato(LocalDate fodselsdato) {
        this.fodselsdato = fodselsdato;
    }

    public String getAlder() {
        return alder;
    }

    public void setAlder(String alder) {
        this.alder = alder;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getFornavn() {
        return fornavn;
    }

    public void setFornavn(String fornavn) {
        this.fornavn = fornavn;
    }

    public String getMellomnavn() {
        return mellomnavn;
    }

    public void setMellomnavn(String mellomnavn) {
        this.mellomnavn = mellomnavn;
    }

    public String getEtternavn() {
        return etternavn;
    }

    public void setEtternavn(String etternavn) {
        this.etternavn = etternavn;
    }

    public String getDiskresjonskode() {
        return diskresjonskode;
    }

    public void setDiskresjonskode(String diskresjonskode) {
        this.diskresjonskode = diskresjonskode;
    }

    public String getMobiltelefonnummer() {
        return mobiltelefonnummer;
    }

    public void setMobiltelefonnummer(String mobiltelefonnummer) {
        this.mobiltelefonnummer = mobiltelefonnummer;
    }

    public String getEpost() {
        return epost;
    }

    public void setEpost(String epost) {
        this.epost = epost;
    }

    public String getStatsborgerskap() {
        return statsborgerskap;
    }

    public void setStatsborgerskap(String statsborgerskap) {
        this.statsborgerskap = statsborgerskap;
    }

    public String getKjonn() {
        return kjonn;
    }

    public void setKjonn(String kjonn) {
        this.kjonn = kjonn;
    }

    public Adresse getGjeldendeAdresse() {
        return gjeldendeAdresse;
    }

    public void setGjeldendeAdresse(Adresse gjeldendeAdresse) {
        this.gjeldendeAdresse = gjeldendeAdresse;
    }

    public Adresse getMidlertidigAdresse() { return midlertidigAdresse; }

    public void setMidlertidigAdresse(Adresse midlertidigAdresse) { this.midlertidigAdresse = midlertidigAdresse; }

    public Adresse getSekundarAdresse() {
        return sekundarAdresse;
    }

    public void setSekundarAdresse(Adresse sekundarAdresse) {
        this.sekundarAdresse = sekundarAdresse;
    }

    public Adresse getFolkeregistrertAdresse() {
        return folkeregistrertAdresse;
    }

    public void setFolkeregistrertAdresse(Adresse folkeregistrertAdresse) {
        this.folkeregistrertAdresse = folkeregistrertAdresse;
    }

    public boolean harUtenlandskAdresse() {
        String adressetype = null;

        if (gjeldendeAdresse != null) {
            adressetype = gjeldendeAdresse.getAdressetype();
        }

        if (adressetype == null) {
            return false;
        }

        return harUtenlandsAdressekode(adressetype);
    }

    public String erBosattIEOSLand() {
        return String.valueOf(!harUtenlandskAdresse() || harUtenlandskAdresseIEOS());
    }

    public boolean harUtenlandskAdresseIEOS() {

        String adressetype = null;
        String landkode = null;
        if (gjeldendeAdresse != null) {
            adressetype = gjeldendeAdresse.getAdressetype();
            landkode = gjeldendeAdresse.getLandkode();
        }

        if (adressetype == null) {
            return false;
        }

        return (harUtenlandsAdressekode(adressetype)) && (StatsborgerskapType.get(landkode).equals(EOS));
    }

    private boolean harUtenlandsAdressekode(String adressetype) {
        return adressetype.equalsIgnoreCase(MIDLERTIDIG_POSTADRESSE_UTLAND.name()) ||
                adressetype.equalsIgnoreCase(POSTADRESSE_UTLAND.name()) ||
                adressetype.equalsIgnoreCase(UTENLANDSK_ADRESSE.name());
    }

    public String getKontonummer() {
        return kontonummer;
    }

    public void setKontonummer(String kontonummer) {
        this.kontonummer = kontonummer;

    }

    public Boolean getErUtenlandskBankkonto() {
        return erUtenlandskBankkonto;
    }

    public void setErUtenlandskBankkonto(Boolean erUtenlandskBankkonto) {
        this.erUtenlandskBankkonto = erUtenlandskBankkonto;
    }

    public String getUtenlandskKontoBanknavn() {
        return utenlandskKontoBanknavn;
    }

    public void setUtenlandskKontoBanknavn(String utenlandskKontoBanknavn) {
        this.utenlandskKontoBanknavn = utenlandskKontoBanknavn;
    }

    public String getUtenlandskKontoLand() {
        return utenlandskKontoLand;
    }

    public void setUtenlandskKontoLand(String utenlandskKontoLand) {
        this.utenlandskKontoLand = utenlandskKontoLand;
    }

    public String getSivilstatus() {
        return sivilstatus;
    }

    public void setSivilstatus(String sivilstatus) {
        this.sivilstatus = sivilstatus;
    }

    public Ektefelle getEktefelle() {
        return ektefelle;
    }

    public void setEktefelle(Ektefelle ektefelle) {
        this.ektefelle = ektefelle;
    }
}
