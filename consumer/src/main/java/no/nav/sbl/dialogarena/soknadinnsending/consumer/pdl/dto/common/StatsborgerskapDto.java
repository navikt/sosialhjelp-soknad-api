package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StatsborgerskapDto {

    private final String land;

    @JsonCreator
    public StatsborgerskapDto(
            @JsonProperty("land") String land
    ) {
        this.land = land;
    }

    public String getLand() {
        return land;
    }
}
