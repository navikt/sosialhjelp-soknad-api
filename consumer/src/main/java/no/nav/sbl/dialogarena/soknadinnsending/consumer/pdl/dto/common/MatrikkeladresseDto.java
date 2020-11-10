package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MatrikkeladresseDto {

    private final String matrikkelId;
    private final String postnummer;
    private final String tilleggsnavn;
    private final String kommunenummer;
    private final String bruksenhetsnummer;

    @JsonCreator
    public MatrikkeladresseDto(
            @JsonProperty("matrikkelId") String matrikkelId,
            @JsonProperty("postnummer") String postnummer,
            @JsonProperty("tilleggsnavn") String tilleggsnavn,
            @JsonProperty("kommunenummer") String kommunenummer,
            @JsonProperty("bruksenhetsnummer") String bruksenhetsnummer) {
        this.matrikkelId = matrikkelId;
        this.postnummer = postnummer;
        this.tilleggsnavn = tilleggsnavn;
        this.kommunenummer = kommunenummer;
        this.bruksenhetsnummer = bruksenhetsnummer;
    }

    public String getMatrikkelId() {
        return matrikkelId;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getTilleggsnavn() {
        return tilleggsnavn;
    }

    public String getKommunenummer() {
        return kommunenummer;
    }

    public String getBruksenhetsnummer() {
        return bruksenhetsnummer;
    }

}
