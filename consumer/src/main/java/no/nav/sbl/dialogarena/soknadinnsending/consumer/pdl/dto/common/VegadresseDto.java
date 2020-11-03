package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VegadresseDto that = (VegadresseDto) o;
        return Objects.equals(matrikkelId, that.matrikkelId) &&
                Objects.equals(adressenavn, that.adressenavn) &&
                Objects.equals(husnummer, that.husnummer) &&
                Objects.equals(husbokstav, that.husbokstav) &&
                Objects.equals(tilleggsnavn, that.tilleggsnavn) &&
                Objects.equals(postnummer, that.postnummer) &&
                Objects.equals(kommunenummer, that.kommunenummer) &&
                Objects.equals(bruksenhetsnummer, that.bruksenhetsnummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matrikkelId, adressenavn, husnummer, husbokstav, tilleggsnavn, postnummer, kommunenummer, bruksenhetsnummer);
    }
}
