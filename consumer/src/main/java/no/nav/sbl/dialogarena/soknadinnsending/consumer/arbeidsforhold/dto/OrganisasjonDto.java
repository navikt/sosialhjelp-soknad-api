package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisasjonDto extends OpplysningspliktigArbeidsgiverDto {

    private String organisasjonsnummer;
    private String type;

    @JsonCreator
    public OrganisasjonDto(
            @JsonProperty("organisasjonsnummer") String organisasjonsnummer,
            @JsonProperty("type") String type) {
        this.organisasjonsnummer = organisasjonsnummer;
        this.type = type;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public String getType() {
        return type;
    }
}
