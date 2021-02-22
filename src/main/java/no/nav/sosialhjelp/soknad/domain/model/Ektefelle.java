package no.nav.sosialhjelp.soknad.domain.model;

import org.joda.time.LocalDate;

public class Ektefelle {
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private LocalDate fodselsdato;
    private String fnr;
    private boolean folkeregistrertsammen;
    private boolean ikketilgangtilektefelle;

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

    public boolean erFolkeregistrertsammen() {
        return folkeregistrertsammen;
    }

    public boolean harIkketilgangtilektefelle() {
        return ikketilgangtilektefelle;
    }

    public Ektefelle withFornavn(String fornavn) {
        this.fornavn = fornavn;
        return this;
    }

    public Ektefelle withMellomnavn(String mellomnavn) {
        this.mellomnavn = mellomnavn;
        return this;
    }

    public Ektefelle withEtternavn(String etternavn) {
        this.etternavn = etternavn;
        return this;
    }

    public Ektefelle withFodselsdato(LocalDate fodselsdato) {
        this.fodselsdato = fodselsdato;
        return this;
    }

    public Ektefelle withFnr(String fnr) {
        this.fnr = fnr;
        return this;
    }

    public Ektefelle withFolkeregistrertsammen(boolean folkeregistrertsammen) {
        this.folkeregistrertsammen = folkeregistrertsammen;
        return this;
    }

    public Ektefelle withIkketilgangtilektefelle(boolean ikketilgangtilektefelle) {
        this.ikketilgangtilektefelle = ikketilgangtilektefelle;
        return this;
    }
}
