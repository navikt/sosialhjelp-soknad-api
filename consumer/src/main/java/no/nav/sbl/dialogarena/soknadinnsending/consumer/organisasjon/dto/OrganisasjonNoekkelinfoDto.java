package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class OrganisasjonNoekkelinfoDto {

    private NavnDto navn;
    private String organisasjonsnummer;

    @JsonCreator
    public OrganisasjonNoekkelinfoDto(
            @JsonProperty("navn") NavnDto navn,
            @JsonProperty("organisasjonsnummer") String organisasjonsnummer) {
        this.navn = navn;
        this.organisasjonsnummer = organisasjonsnummer;
    }

    public NavnDto getNavn() {
        return navn;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

}