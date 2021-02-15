package no.nav.sosialhjelp.soknad.consumer.organisasjon.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class OrganisasjonNoekkelinfoDto {

    private final NavnDto navn;
    private final String organisasjonsnummer;

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