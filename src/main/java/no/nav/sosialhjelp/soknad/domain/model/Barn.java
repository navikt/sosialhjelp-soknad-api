package no.nav.sosialhjelp.soknad.domain.model;

import java.time.LocalDate;

public class Barn {
    private String fnr;
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private LocalDate fodselsdato;
    private Boolean folkeregistrertsammen;

    public String getFnr() {
        return fnr;
    }

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

    public Boolean erFolkeregistrertsammen() {
        return folkeregistrertsammen;
    }

    public Barn withFnr(String fnr) {
        this.fnr = fnr;
        return this;
    }

    public Barn withFornavn(String fornavn) {
        this.fornavn = fornavn;
        return this;
    }

    public Barn withMellomnavn(String mellomnavn) {
        this.mellomnavn = mellomnavn;
        return this;
    }

    public Barn withEtternavn(String etternavn) {
        this.etternavn = etternavn;
        return this;
    }

    public Barn withFodselsdato(LocalDate fodselsdato) {
        this.fodselsdato = fodselsdato;
        return this;
    }

    public Barn withFolkeregistrertsammen(Boolean folkeregistrertsammen) {
        this.folkeregistrertsammen = folkeregistrertsammen;
        return this;
    }
}
