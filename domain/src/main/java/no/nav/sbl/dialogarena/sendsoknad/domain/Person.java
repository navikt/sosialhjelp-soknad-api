package no.nav.sbl.dialogarena.sendsoknad.domain;

import org.joda.time.LocalDate;

public class Person {
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private LocalDate fodselsdato;
    private String fnr;
    private String alder;
    private String kjonn;
    private String sivilstatus;
    private String diskresjonskode;
    private String statsborgerskap;
    private Ektefelle ektefelle;

    public String getFornavn() {
        return fornavn;
    }

    public String getMellomnavn() {
        return mellomnavn;
    }

    public String getEtternavn() {
        return etternavn;
    }

    public LocalDate getFodselsdato() {
        return fodselsdato;
    }

    public String getFnr() {
        return fnr;
    }

    public String getAlder() {
        return alder;
    }

    public String getKjonn() {
        return kjonn;
    }

    public String getSivilstatus() {
        return sivilstatus;
    }

    public String getDiskresjonskode() {
        return diskresjonskode;
    }

    public String getStatsborgerskap() {
        return statsborgerskap;
    }

    public Ektefelle getEktefelle() {
        return ektefelle;
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

    public Person withFodselsdato(LocalDate fodselsdato) {
        this.fodselsdato = fodselsdato;
        return this;
    }

    public Person withFnr(String fnr) {
        this.fnr = fnr;
        return this;
    }

    public Person withAlder(String alder) {
        this.alder = alder;
        return this;
    }

    public Person withKjonn(String kjonn) {
        this.kjonn = kjonn;
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

    public Person withStatsborgerskap(String statsborgerskap) {
        this.statsborgerskap = statsborgerskap;
        return this;
    }

    public Person withEktefelle(Ektefelle ektefelle) {
        this.ektefelle = ektefelle;
        return this;
    }
}
