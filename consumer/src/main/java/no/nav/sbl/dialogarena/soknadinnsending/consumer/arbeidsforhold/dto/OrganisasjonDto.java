package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisasjonDto extends OpplysningspliktigArbeidsgiverDto {

    private String organisasjonsnummer;

    public OrganisasjonDto(String organisasjonsnummer) {
        this.organisasjonsnummer = organisasjonsnummer;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    @Override
    public String getType() {
        return "Organisasjon";
    }
}
