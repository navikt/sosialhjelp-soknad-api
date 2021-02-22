package no.nav.sosialhjelp.soknad.consumer.person.domain;

public class UstrukturertAdresse {

    private String adresselinje1;
    private String adresselinje2;
    private String adresselinje3;
    private String adresselinje4;
    private String landkode;

    public String getAdresselinje1() { return adresselinje1; }

    public String getAdresselinje2() { return adresselinje2; }

    public String getAdresselinje3() { return adresselinje3; }

    public String getAdresselinje4() { return adresselinje4; }

    public String getLandkode() { return landkode; }

    public UstrukturertAdresse withAdresselinje1(String adresselinje1) {
        this.adresselinje1 = adresselinje1;
        return this;
    }

    public UstrukturertAdresse withAdresselinje2(String adresselinje2) {
        this.adresselinje2 = adresselinje2;
        return this;
    }

    public UstrukturertAdresse withAdresselinje3(String adresselinje3) {
        this.adresselinje3 = adresselinje3;
        return this;
    }

    public UstrukturertAdresse withAdresselinje4(String adresselinje4) {
        this.adresselinje4 = adresselinje4;
        return this;
    }

    public UstrukturertAdresse withLandkode(String landkode) {
        this.landkode = landkode;
        return this;
    }
}
