package no.nav.sosialhjelp.soknad.consumer.pdl.person.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndringDto {

    private final String kilde;
    private final LocalDateTime registrert;
    private final String type;

    @JsonCreator
    public EndringDto(
            @JsonProperty("kilde") String kilde,
            @JsonProperty("registrert") LocalDateTime registrert,
            @JsonProperty("type") String type) {
        this.kilde = kilde;
        this.registrert = registrert;
        this.type = type;
    }

    public String getKilde() {
        return kilde;
    }

    public LocalDateTime getRegistrert() {
        return registrert;
    }

    public String getType() {
        return type;
    }
}
