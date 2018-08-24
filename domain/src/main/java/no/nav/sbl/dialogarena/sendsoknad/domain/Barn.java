package no.nav.sbl.dialogarena.sendsoknad.domain;

import org.joda.time.LocalDate;

public class Barn {
    private String fnr;
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private LocalDate fodselsdato;
    private boolean folkeregistrertsammen;
    private boolean ikkeTilgang;
    private boolean utvandret;

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

    public boolean erFolkeregistrertsammen() {
        return folkeregistrertsammen;
    }

    public boolean harIkkeTilgang() {
        return ikkeTilgang;
    }

    public boolean erUtvandret() {
        return utvandret;
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

    public Barn withFolkeregistrertsammen(boolean folkeregistrertsammen) {
        this.folkeregistrertsammen = folkeregistrertsammen;
        return this;
    }

    public Barn withIkkeTilgang(boolean ikkeTilgang) {
        this.ikkeTilgang = ikkeTilgang;
        return this;
    }

    public Barn withUtvandret(boolean utvandret) {
        this.utvandret = utvandret;
        return this;
    }
}
