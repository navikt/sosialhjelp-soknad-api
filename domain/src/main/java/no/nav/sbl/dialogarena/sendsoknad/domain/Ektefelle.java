package no.nav.sbl.dialogarena.sendsoknad.domain;

import org.joda.time.LocalDate;

public class Ektefelle {
    private String navn;
    private LocalDate fodselsdato;
    private String fnr;
    private boolean folkeregistrertsammen;
    private boolean ikketilgangtilektefelle;

    public String getNavn() {
        return navn;
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

    public Ektefelle withNavn(String navn) {
        this.navn = navn;
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
