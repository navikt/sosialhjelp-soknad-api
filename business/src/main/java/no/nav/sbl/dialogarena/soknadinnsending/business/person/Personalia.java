package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.UTENLANDSK_ADRESSE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.MIDLERTIDIG_POSTADRESSE_UTLAND;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.POSTADRESSE_UTLAND;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.POSTADRESSE;

public class Personalia {

    public static final String PERSONALIA_KEY = "personalia";
    public static final String FNR_KEY = "fnr";
    public static final String ALDER_KEY = "alder";
    public static final String EPOST_KEY = "epost";
    public static final String STATSBORGERSKAP_KEY = "statsborgerskap";
    public static final String NAVN_KEY = "navn";
    public static final String KJONN_KEY = "kjonn";
    public static final String GJELDENDEADRESSE_KEY = "gjeldendeAdresse";
    public static final String GJELDENDEADRESSE_TYPE_KEY = "gjeldendeAdresseType";
    public static final String GJELDENDEADRESSE_GYLDIGFRA_KEY = "gjeldendeAdresseGydligFra";
    public static final String GJELDENDEADRESSE_GYLDIGTIL_KEY = "gjeldendeAdresseGydligTil";
    public static final String SEKUNDARADRESSE_KEY = "sekundarAdresse";
    public static final String SEKUNDARADRESSE_TYPE_KEY = "sekundarAdresseType";
    public static final String SEKUNDARADRESSE_GYLDIGFRA_KEY = "sekundarAdresseGydligFra";
    public static final String SEKUNDARADRESSE_GYLDIGTIL_KEY = "sekundarAdresseGydligTil";

    private String fnr;
    private String alder;
    private String navn;
    private String epost;
    private String statsborgerskap;
    private String kjonn;
    private Adresse gjeldendeAdresse;
    private Adresse sekundarAdresse;

    public Personalia() {
    }

    public String getFnr() {
        return fnr;
    }

    public void setFnr(String fnr) {
        this.fnr = fnr;
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
        String adressetype = gjeldendeAdresse.getAdressetype();

        if (adressetype == null) {
            return false;
        }

        if (adressetype.equalsIgnoreCase(MIDLERTIDIG_POSTADRESSE_UTLAND.name()) || adressetype.equalsIgnoreCase(POSTADRESSE_UTLAND.name())) {
            return true;
        } else if (adressetype.equalsIgnoreCase(POSTADRESSE.name())) {
            return harUtenlandskFolkeregistrertAdresse();
        }
        return false;
    }

    public boolean harNorskMidlertidigAdresse() {
        if (sekundarAdresse == null)
        {
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


    @SuppressWarnings("unchecked")
    public boolean harUtenlandskFolkeregistrertAdresse() {

        if ((gjeldendeAdresse == null) || (gjeldendeAdresse.getAdressetype() == null))
        {
            return false;
        }
        if ((sekundarAdresse == null) || (sekundarAdresse.getAdressetype() == null))
        {
            return false;
        }
        if (gjeldendeAdresse.getAdressetype().equalsIgnoreCase(UTENLANDSK_ADRESSE.name()) || sekundarAdresse.getAdressetype().equalsIgnoreCase(UTENLANDSK_ADRESSE.name())) {
            return true;
        }
        return false;
    }
}
