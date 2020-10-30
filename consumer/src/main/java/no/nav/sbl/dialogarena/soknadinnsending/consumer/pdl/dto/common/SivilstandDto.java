package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SivilstandDto {

    private final SivilstandType type;
    private final String relatertVedSivilstand;

    @JsonCreator
    public SivilstandDto(
            @JsonProperty("type") SivilstandType type,
            @JsonProperty("relatertVedSivilstand") String relatertVedSivilstand
    ) {
        this.type = type;
        this.relatertVedSivilstand = relatertVedSivilstand;
    }

    public SivilstandType getType() {
        return type;
    }

    public String getRelatertVedSivilstand() {
        return relatertVedSivilstand;
    }

    public enum SivilstandType {
        UOPPGITT, UGIFT, GIFT, ENKE_ELLER_ENKEMANN, SKILT, SEPARERT, PARTNER, SEPARERT_PARTNER, SKILT_PARTNER, GJENLEVENDE_PARTNER
    }
}
