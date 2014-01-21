package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Adressetype.UTENLANDSK_ADRESSE;

public class Personalia {

    private String fnr;
    private String alder;
    private String navn;
    private String epost;
    private String statsborgerskap;
    private String kjonn;
    private NewAdresse gjeldendeAdresse;
    private NewAdresse sekundarAdresse;

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

    public NewAdresse getGjeldendeAdresse() {
        return gjeldendeAdresse;
    }

    public void setGjeldendeAdresse(NewAdresse gjeldendeAdresse) {
        this.gjeldendeAdresse = gjeldendeAdresse;
    }

    public NewAdresse getSekundarAdresse() {
        return sekundarAdresse;
    }

    public void setSekundarAdresse(NewAdresse sekundarAdresse) {
        this.sekundarAdresse = sekundarAdresse;
    }

    public boolean harUtenlandskAdresse() {
        String adressetype = gjeldendeAdresse.getAdressetype();

        if (adressetype == null) {
            return false;
        }

        if (adressetype.equalsIgnoreCase(Adressetype.MIDLERTIDIG_POSTADRESSE_UTLAND.name()) || adressetype.equalsIgnoreCase(Adressetype.POSTADRESSE_UTLAND.name())) {
            return true;
        } else if (adressetype.equalsIgnoreCase(Adressetype.POSTADRESSE.name())) {
            return erUtenlandskFolkeregistrertAdresse();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean erUtenlandskFolkeregistrertAdresse() {
        if (gjeldendeAdresse.getAdressetype().equalsIgnoreCase(UTENLANDSK_ADRESSE.name()) || sekundarAdresse.getAdressetype().equalsIgnoreCase(UTENLANDSK_ADRESSE.name())) {
            return true;
        }
        return false;
    }
}
