package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.soknadinnsending.business.util.StatsborgerskapType;
import org.joda.time.LocalDate;

import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.LandService.EOS;


public class Personalia {

    public static final String PERSONALIA_KEY = "personalia";
    public static final String FNR_KEY = "fnr";
    public static final String KONTONUMMER_KEY = "kontonummer";
    public static final String ER_UTENLANDSK_BANKKONTO = "erUtenlandskBankkonto";
    public static final String UTENLANDSK_KONTO_BANKNAVN = "utenlandskKontoBanknavn";
    public static final String UTENLANDSK_KONTO_LAND = "utenlandskKontoLand";
    public static final String ALDER_KEY = "alder";
    public static final String EPOST_KEY = "epost";
    public static final String STATSBORGERSKAP_KEY = "statsborgerskap";
    public static final String STATSBORGERSKAPTYPE_KEY = "statsborgerskapType";
    public static final String NAVN_KEY = "navn";
    public static final String KJONN_KEY = "kjonn";
    public static final String GJELDENDEADRESSE_KEY = "gjeldendeAdresse";
    public static final String DISKRESJONSKODE = "diskresjonskode";
    public static final String GJELDENDEADRESSE_TYPE_KEY = "gjeldendeAdresseType";
    public static final String GJELDENDEADRESSE_GYLDIGFRA_KEY = "gjeldendeAdresseGyldigFra";
    public static final String GJELDENDEADRESSE_GYLDIGTIL_KEY = "gjeldendeAdresseGyldigTil";
    public static final String GJELDENDEADRESSE_LANDKODE = "gjeldendeAdresseLandkode";
    public static final String SEKUNDARADRESSE_KEY = "sekundarAdresse";
    public static final String SEKUNDARADRESSE_TYPE_KEY = "sekundarAdresseType";
    public static final String SEKUNDARADRESSE_GYLDIGFRA_KEY = "sekundarAdresseGyldigFra";
    public static final String SEKUNDARADRESSE_GYLDIGTIL_KEY = "sekundarAdresseGyldigTil";
    public static final String SEKUNDARADRESSE_LANDKODE = "sekundarAdresseLandkode";

    private String fnr;
    private LocalDate fodselsdato;
    private String alder;
    private String navn;
    private String epost;
    private String statsborgerskap;
    private String kjonn;
    private Adresse gjeldendeAdresse;
    private Adresse sekundarAdresse;
    private String kontonummer;
    private String diskresjonskode;
    private Boolean erUtenlandskBankkonto;
    private String utenlandskKontoBanknavn;
    private String utenlandskKontoLand;

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

    public String getDiskresjonskode() {
        return diskresjonskode;
    }

    public void setDiskresjonskode(String diskresjonskode) {
        this.diskresjonskode = diskresjonskode;
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

    public Adresse getSekundarAdresse() {
        return sekundarAdresse;
    }

    public void setSekundarAdresse(Adresse sekundarAdresse) {
        this.sekundarAdresse = sekundarAdresse;
    }

    public boolean harUtenlandskAdresse() {
        String adressetype = null;

        if (gjeldendeAdresse != null) {
            adressetype = gjeldendeAdresse.getAdressetype();
        }

        if (adressetype == null) {
            return false;
        }

        if (harUtenlandsAdressekode(adressetype)) {
            return true;
        }
        return false;
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

        if ((harUtenlandsAdressekode(adressetype)) && (StatsborgerskapType.get(landkode).equals(EOS))) {
            return true;
        }
        return false;
    }

    private boolean harUtenlandsAdressekode(String adressetype) {
        if (adressetype.equalsIgnoreCase(MIDLERTIDIG_POSTADRESSE_UTLAND.name()) ||
                adressetype.equalsIgnoreCase(POSTADRESSE_UTLAND.name()) ||
                adressetype.equalsIgnoreCase(UTENLANDSK_ADRESSE.name())) {
            return true;
        }
        return false;
    }

    public boolean harNorskMidlertidigAdresse() {
        if (sekundarAdresse == null) {
            return false;
        }
        String adressetype = sekundarAdresse.getAdressetype();

        if (adressetype == null) {
            return false;
        }
        if (adressetype.equalsIgnoreCase(MIDLERTIDIG_POSTADRESSE_NORGE.name())) {
            return true;
        }
        return false;
    }

    public boolean harUtenlandskFolkeregistrertAdresse() {
        if ((gjeldendeAdresse == null) || (gjeldendeAdresse.getAdressetype() == null)) {
            return false;
        }
        return gjeldendeAdresse.getAdressetype().equalsIgnoreCase(UTENLANDSK_ADRESSE.name());
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
}
