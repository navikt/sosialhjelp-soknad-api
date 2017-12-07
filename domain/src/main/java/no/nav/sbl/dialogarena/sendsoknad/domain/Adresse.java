package no.nav.sbl.dialogarena.sendsoknad.domain;

public class Adresse {
    private String adressetype;
    private String gyldigTil;
    private String gyldigFra;
    private String adresseString;
    private String landkode;
    private StrukturertAdresse strukturertAdresse;

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

    public StrukturertAdresse getStrukturertAdresse() {
        return strukturertAdresse;
    }

    public void setStrukturertAdresse(StrukturertAdresse strukturertAdresse) {
        this.strukturertAdresse = strukturertAdresse;
    }

    public static abstract class StrukturertAdresse {
        public String type;
        public String kommunenummer;
        public String bolignummer;
        public String poststed;
    }

    public static class Gateadresse extends StrukturertAdresse {
       public String gatenavn;
       public String husnummer;

        public Gateadresse() {
            this.type = "gateadresse";
        }

    }

    public static class MatrikkelAdresse extends StrukturertAdresse {
        public String eiendomsnavn;
        public String gaardsnummer;
        public String bruksnummer;
        public String festenummer;
        public String seksjonsnummer;
        public String undernummer;

        public MatrikkelAdresse() {
            this.type = "matrikkeladresse";
        }

    }
}
