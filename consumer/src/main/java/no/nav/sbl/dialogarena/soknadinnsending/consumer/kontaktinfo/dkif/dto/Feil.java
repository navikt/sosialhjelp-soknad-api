package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feil {

    private String melding;

    @JsonCreator
    public Feil(@JsonProperty("melding") String melding) {
        this.melding = melding;
    }

    public String getMelding() {
        return melding;
    }
}
