package no.nav.sosialhjelp.soknad.consumer.pdl.adressesok;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AdresseDto {

    private final String matrikkelId;
    private final Integer husnummer;
    private final String husbokstav;
    private final String adressenavn;
    private final String tilleggsnavn;
    private final String fylkenavn;
    private final String fylkenummer;
    private final String kommunenavn;
    private final String kommunenummer;
    private final String postnummer;
    private final String poststed;
    private final String bydelsnavn;
    private final String bydelsnummer;

    @JsonCreator
    public AdresseDto(
            @JsonProperty("matrikkelId") String matrikkelId,
            @JsonProperty("husnummer") Integer husnummer,
            @JsonProperty("husbokstav") String husbokstav,
            @JsonProperty("adressenavn") String adressenavn,
            @JsonProperty("tilleggsnavn") String tilleggsnavn,
            @JsonProperty("fylkenavn") String fylkenavn,
            @JsonProperty("fylkenummer") String fylkenummer,
            @JsonProperty("kommunenavn") String kommunenavn,
            @JsonProperty("kommunenummer") String kommunenummer,
            @JsonProperty("postnummer") String postnummer,
            @JsonProperty("poststed") String poststed,
            @JsonProperty("bydelsnavn") String bydelsnavn,
            @JsonProperty("bydelsnummer") String bydelsnummer
    ) {
        this.matrikkelId = matrikkelId;
        this.husnummer = husnummer;
        this.husbokstav = husbokstav;
        this.adressenavn = adressenavn;
        this.tilleggsnavn = tilleggsnavn;
        this.fylkenavn = fylkenavn;
        this.fylkenummer = fylkenummer;
        this.kommunenavn = kommunenavn;
        this.kommunenummer = kommunenummer;
        this.postnummer = postnummer;
        this.poststed = poststed;
        this.bydelsnavn = bydelsnavn;
        this.bydelsnummer = bydelsnummer;
    }

    public String getMatrikkelId() {
        return matrikkelId;
    }

    public Integer getHusnummer() {
        return husnummer;
    }

    public String getHusbokstav() {
        return husbokstav;
    }

    public String getAdressenavn() {
        return adressenavn;
    }

    public String getTilleggsnavn() {
        return tilleggsnavn;
    }

    public String getFylkenavn() {
        return fylkenavn;
    }

    public String getFylkenummer() {
        return fylkenummer;
    }

    public String getKommunenavn() {
        return kommunenavn;
    }

    public String getKommunenummer() {
        return kommunenummer;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getPoststed() {
        return poststed;
    }

    public String getBydelsnavn() {
        return bydelsnavn;
    }

    public String getBydelsnummer() {
        return bydelsnummer;
    }
}
