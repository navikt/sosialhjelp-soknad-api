package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FolkeregisterMetadataDto {

    private final LocalDateTime ajourholdstidspunkt;
    private final LocalDateTime gyldighetstidspunkt;
    private final LocalDateTime opphoerstidspunkt;
    private final String kilde;
    private final String aarsak;
    private final Integer sekvens;

    @JsonCreator
    public FolkeregisterMetadataDto(
            @JsonProperty("ajourholdstidspunkt") LocalDateTime ajourholdstidspunkt,
            @JsonProperty("gyldighetstidspunkt") LocalDateTime gyldighetstidspunkt,
            @JsonProperty("opphoerstidspunkt") LocalDateTime opphoerstidspunkt,
            @JsonProperty("kilde") String kilde,
            @JsonProperty("aarsak") String aarsak,
            @JsonProperty("sekvens") Integer sekvens) {
        this.ajourholdstidspunkt = ajourholdstidspunkt;
        this.gyldighetstidspunkt = gyldighetstidspunkt;
        this.opphoerstidspunkt = opphoerstidspunkt;
        this.kilde = kilde;
        this.aarsak = aarsak;
        this.sekvens = sekvens;
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

    public String getAarsak() {
        return aarsak;
    }

    public Integer getSekvens() {
        return sekvens;
    }
}
