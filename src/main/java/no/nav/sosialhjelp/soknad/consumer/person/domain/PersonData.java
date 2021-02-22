package no.nav.sosialhjelp.soknad.consumer.person.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.ks.svarut.servicesv9.PostAdresse;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public class PersonData {

    private String diskresjonskode;
    private String kontonummer;
    private Bostedsadresse bostedsadresse;
    private MidlertidigAdresseNorge midlertidigAdresseNorge;
    private MidlertidigAdresseUtland midlertidigAdresseUtland;
    private PostAdresse postAdresse;
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String fodselsnummer;

    @JsonIgnore
    public Optional<String> getPostnummerForBostedsadresse() {
        return ofNullable(bostedsadresse)
                .map(Bostedsadresse::getStrukturertAdresse)
                .map(StrukturertAdresse::getPostnummer);
    }

    public void setPoststedForBostedsadresse(String poststed) {
        ofNullable(bostedsadresse)
                .map(Bostedsadresse::getStrukturertAdresse)
                .ifPresent(strukturertAdresse -> strukturertAdresse.withPoststed(poststed));
    }

    @JsonIgnore
    public Optional<String> getPostnummerForMidlertidigAdresseNorge() {
        return ofNullable(midlertidigAdresseNorge)
                .map(MidlertidigAdresseNorge::getStrukturertAdresse)
                .map(StrukturertAdresse::getPostnummer);

    }

    public void setPoststedForMidlertidigAdresseNorge(String poststed) {
        ofNullable(midlertidigAdresseNorge)
                .map(MidlertidigAdresseNorge::getStrukturertAdresse)
                .ifPresent(strukturertAdresse -> strukturertAdresse.withPoststed(poststed));
    }

    public PersonData withDiskresjonskode(String diskresjonskode) {
        this.diskresjonskode = diskresjonskode;
        return this;
    }

    public PersonData withKontonummer(String kontonummer) {
        this.kontonummer = kontonummer;
        return this;
    }

    public PersonData withBostedsadresse(Bostedsadresse bostedsadresse) {
        this.bostedsadresse = bostedsadresse;
        return this;
    }

    public PersonData withMidlertidigAdresseNorge(MidlertidigAdresseNorge midlertidigAdresseNorge) {
        this.midlertidigAdresseNorge = midlertidigAdresseNorge;
        return this;
    }

    public PersonData withMidlertidigAdresseUtland(MidlertidigAdresseUtland midlertidigAdresseUtland) {
        this.midlertidigAdresseUtland = midlertidigAdresseUtland;
        return this;
    }


    public PersonData withFornavn(String fornavn) {
        this.fornavn = fornavn;
        return this;
    }

    public PersonData withMellomnavn(String mellomnavn) {
        this.mellomnavn = mellomnavn;
        return this;
    }

    public PersonData withEtternavn(String etternavn) {
        this.etternavn = etternavn;
        return this;
    }

    public PersonData withFodselsnummer(String fodselsnummer) {
        this.fodselsnummer = fodselsnummer;
        return this;
    }

    public PersonData withPostAdresse(PostAdresse postAdresse) {
        this.postAdresse = postAdresse;
        return this;
    }

    public String getDiskresjonskode() {
        return diskresjonskode;
    }

    public String getKontonummer() {
        return kontonummer;
    }

    public Bostedsadresse getBostedsadresse() {
        return bostedsadresse;
    }

    public MidlertidigAdresseNorge getMidlertidigAdresseNorge() {
        return midlertidigAdresseNorge;
    }

    public MidlertidigAdresseUtland getMidlertidigAdresseUtland() {
        return midlertidigAdresseUtland;
    }

    public PostAdresse getPostAdresse() {
        return postAdresse;
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

    public String getFodselsnummer() {
        return fodselsnummer;
    }

}