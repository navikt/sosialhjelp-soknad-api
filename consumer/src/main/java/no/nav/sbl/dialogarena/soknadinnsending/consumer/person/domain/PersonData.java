package no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.ks.svarut.servicesv9.PostAdresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Sivilstand;

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
    private String statsborgerskap;
    private Ektefelle ektefelle;
    private Sivilstand sivilStand;

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
    public PersonData withStatsborgerskap(String statsborgerskap) {
        this.statsborgerskap = statsborgerskap;
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

    public PersonData withEktefelle(Ektefelle ektefelle) {
        this.ektefelle = ektefelle;
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

    public String getStatsborgerskap() {
        return statsborgerskap;
    }

    public void setSivilstand(Sivilstand sivilstand) {
        this.sivilStand = sivilstand;
    }

    public PersonData withSivilstand(Sivilstand sivilstand) {
        this.sivilStand = sivilstand;
        return this;
    }

    public Sivilstand getSivilStand() {
        return sivilStand;
    }

    public Ektefelle getEktefelle() {
        return ektefelle;
    }
}