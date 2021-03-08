package no.nav.sosialhjelp.soknad.consumer.pdl.person.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FolkeregistermetadataDto {

    private final LocalDateTime ajourholdstidspunkt;
    private final String kilde;

    @JsonCreator
    public FolkeregistermetadataDto(
            @JsonProperty("ajourholdstidspunkt") LocalDateTime ajourholdstidspunkt,
            @JsonProperty("kilde") String kilde
    ) {
        this.ajourholdstidspunkt = ajourholdstidspunkt;
        this.kilde = kilde;
    }

    public LocalDateTime getAjourholdstidspunkt() {
        return ajourholdstidspunkt;
    }

    public String getKilde() {
        return kilde;
    }

}
