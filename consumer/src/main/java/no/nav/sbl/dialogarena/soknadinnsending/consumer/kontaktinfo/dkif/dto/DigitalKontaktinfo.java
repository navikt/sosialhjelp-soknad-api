package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalKontaktinfo {

    private String mobiltelefonnummer;

    @JsonCreator
    public DigitalKontaktinfo(@JsonProperty("mobiltelefonnummer") String mobiltelefonnummer) {
        this.mobiltelefonnummer = mobiltelefonnummer;
    }

    public String getMobiltelefonnummer() {
        return mobiltelefonnummer;
    }
}
