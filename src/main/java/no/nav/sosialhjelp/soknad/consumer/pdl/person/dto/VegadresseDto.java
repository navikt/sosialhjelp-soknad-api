package no.nav.sosialhjelp.soknad.consumer.pdl.person.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VegadresseDto {

    private final String matrikkelId;
    private final String adressenavn;
    private final Integer husnummer;
    private final String husbokstav;
    private final String tilleggsnavn;
    private final String postnummer;
    private final String kommunenummer;
    private final String bruksenhetsnummer;

    @JsonCreator
    public VegadresseDto(
            @JsonProperty("matrikkelId") String matrikkelId,
            @JsonProperty("adressenavn") String adressenavn,
            @JsonProperty("husnummer") Integer husnummer,
            @JsonProperty("husbokstav") String husbokstav,
            @JsonProperty("tilleggsnavn") String tilleggsnavn,
            @JsonProperty("postnummer") String postnummer,
            @JsonProperty("kommunenummer") String kommunenummer,
            @JsonProperty("bruksenhetsnummer") String bruksenhetsnummer
    ) {
        this.matrikkelId = matrikkelId;
        this.adressenavn = adressenavn;
        this.husnummer = husnummer;
        this.husbokstav = husbokstav;
        this.tilleggsnavn = tilleggsnavn;
        this.postnummer = postnummer;
        this.kommunenummer = kommunenummer;
        this.bruksenhetsnummer = bruksenhetsnummer;
    }

    public String getMatrikkelId() {
        return matrikkelId;
    }

    public String getAdressenavn() {
        return adressenavn;
    }

    public Integer getHusnummer() {
        return husnummer;
    }

    public String getHusbokstav() {
        return husbokstav;
    }

    public String getTilleggsnavn() {
        return tilleggsnavn;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getKommunenummer() {
        return kommunenummer;
    }

    public String getBruksenhetsnummer() {
        return bruksenhetsnummer;
    }

}
