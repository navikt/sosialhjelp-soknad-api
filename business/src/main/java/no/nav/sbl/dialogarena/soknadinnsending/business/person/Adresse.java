package no.nav.sbl.dialogarena.soknadinnsending.business.person;

public class Adresse {
    private String adressetype;
    private String gyldigTil;
    private String gyldigFra;
    private String adresseString;
    private String landkode;

    public String getAdressetype() {
        return adressetype;
    }

    public void setAdressetype(String adressetype) {
        this.adressetype = adressetype;
    }

    public String getGyldigTil() {
        return gyldigTil;
    }

    public void setGyldigTil(String gyldigTil) {
        this.gyldigTil = gyldigTil;
    }

    public String getGyldigFra() {
        return gyldigFra;
    }

    public void setGyldigFra(String gyldigFra) {
        this.gyldigFra = gyldigFra;
    }

    public String getAdresse() {
        return adresseString;
    }

    public void setAdresse(String adresse) {
        this.adresseString = adresse;
    }

    public String getLandkode() {
        return landkode;
    }

    public void setLandkode(String landkode) {
        this.landkode = landkode;
    }
}
