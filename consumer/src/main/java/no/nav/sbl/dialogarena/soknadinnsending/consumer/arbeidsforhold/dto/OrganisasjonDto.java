package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = OrganisasjonDto.Builder.class)
public class OrganisasjonDto extends OpplysningspliktigArbeidsgiverDto {

    private final String organisasjonsnummer;

    public OrganisasjonDto(String organisasjonsnummer) {
        this.organisasjonsnummer = organisasjonsnummer;
    }

    public OrganisasjonDto.Builder builder() {
        return new OrganisasjonDto.Builder();
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    @Override
    public String getType() {
        return "Organisasjon";
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static class Builder {
        private String organisasjonsnummer;

        public OrganisasjonDto.Builder withOrganisasjonsnummer(String organisasjonsnummer) {
            this.organisasjonsnummer = organisasjonsnummer;
            return this;
        }

        public OrganisasjonDto build() {
            return new OrganisasjonDto(organisasjonsnummer);
        }
    }
}
