package no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisasjonNoekkelinfoDto {

    private NavnDto navn;
    private String organisasjonsnummer;

    public OrganisasjonNoekkelinfoDto() {
    }

    public OrganisasjonNoekkelinfoDto(NavnDto navn, String organisasjonsnummer) {
        this.navn = navn;
        this.organisasjonsnummer = organisasjonsnummer;
    }

    public NavnDto getNavn() {
        return navn;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }


    public void setNavn(NavnDto navn) {
        this.navn = navn;
    }

    public void setOrganisasjonsnummer(String organisasjonsnummer) {
        this.organisasjonsnummer = organisasjonsnummer;
    }
}