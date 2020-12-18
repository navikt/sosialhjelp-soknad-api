package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FolkeregistermetadataDto {

    private final LocalDateTime ajourholdstidspunkt;
    private final LocalDateTime gyldighetstidspunkt;
    private final LocalDateTime opphoerstidspunkt;
    private final String kilde;

    @JsonCreator
    public FolkeregistermetadataDto(
            @JsonProperty("ajourholdstidspunkt") LocalDateTime ajourholdstidspunkt,
            @JsonProperty("gyldighetstidspunkt") LocalDateTime gyldighetstidspunkt,
            @JsonProperty("opphoerstidspunkt") LocalDateTime opphoerstidspunkt,
            @JsonProperty("kilde") String kilde
    ) {
        this.ajourholdstidspunkt = ajourholdstidspunkt;
        this.gyldighetstidspunkt = gyldighetstidspunkt;
        this.opphoerstidspunkt = opphoerstidspunkt;
        this.kilde = kilde;
    }

    public LocalDateTime getAjourholdstidspunkt() {
        return ajourholdstidspunkt;
    }

    public LocalDateTime getGyldighetstidspunkt() {
        return gyldighetstidspunkt;
    }

    public LocalDateTime getOpphoerstidspunkt() {
        return opphoerstidspunkt;
    }

    public String getKilde() {
        return kilde;
    }

}
