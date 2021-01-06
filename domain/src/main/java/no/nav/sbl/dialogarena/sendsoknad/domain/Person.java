package no.nav.sbl.dialogarena.sendsoknad.domain;

import java.util.List;

public class Person {
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String fnr;
    private String sivilstatus;
    private String diskresjonskode;
    private List<String> statsborgerskap;
    private Ektefelle ektefelle;
    private Bostedsadresse bostedsadresse;
    private Oppholdsadresse oppholdsadresse;
    private Kontaktadresse kontaktadresse;

    public String getFornavn() {
        return fornavn;
    }

    public String getMellomnavn() {
        return mellomnavn;
    }

    public String getEtternavn() {
        return etternavn;
    }

    public String getFnr() {
        return fnr;
    }

    public String getSivilstatus() {
        return sivilstatus;
    }

    public String getDiskresjonskode() {
        return diskresjonskode;
    }

    public List<String> getStatsborgerskap() {
        return statsborgerskap;
    }

    public Ektefelle getEktefelle() {
        return ektefelle;
    }

    public Bostedsadresse getBostedsadresse() {
        return bostedsadresse;
    }

    public Oppholdsadresse getOppholdsadresse() {
        return oppholdsadresse;
    }

    public Kontaktadresse getKontaktadresse() {
        return kontaktadresse;
    }

    public Person withFornavn(String fornavn) {
        this.fornavn = fornavn;
        return this;
    }

    public Person withMellomnavn(String mellomnavn) {
        this.mellomnavn = mellomnavn;
        return this;
    }

    public Person withEtternavn(String etternavn) {
        this.etternavn = etternavn;
        return this;
    }

    public Person withFnr(String fnr) {
        this.fnr = fnr;
        return this;
    }

    public Person withSivilstatus(String sivilstatus) {
        this.sivilstatus = sivilstatus;
        return this;
    }

    public Person withDiskresjonskode(String diskresjonskode) {
        this.diskresjonskode = diskresjonskode;
        return this;
    }

    public Person withStatsborgerskap(List<String> statsborgerskap) {
        this.statsborgerskap = statsborgerskap;
        return this;
    }

    public Person withEktefelle(Ektefelle ektefelle) {
        this.ektefelle = ektefelle;
        return this;
    }

    public Person withBostedsadresse(Bostedsadresse bostedsadresse) {
        this.bostedsadresse = bostedsadresse;
        return this;
    }

    public Person withOppholdsadresse(Oppholdsadresse oppholdsadresse) {
        this.oppholdsadresse = oppholdsadresse;
        return this;
    }

    public Person withKontaktadresse(Kontaktadresse kontaktadresse) {
        this.kontaktadresse = kontaktadresse;
        return this;
    }

}
