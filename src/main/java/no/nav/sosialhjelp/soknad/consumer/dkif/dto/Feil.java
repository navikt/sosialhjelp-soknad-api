package no.nav.sosialhjelp.soknad.consumer.dkif.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Feil {

    private final String melding;

    @JsonCreator
    public Feil(@JsonProperty("melding") String melding) {
        this.melding = melding;
    }

    public String getMelding() {
        return melding;
    }

    @Override
    public String toString() {
        return "Feil{" +
                "melding='" + melding + '\'' +
                '}';
    }
}
