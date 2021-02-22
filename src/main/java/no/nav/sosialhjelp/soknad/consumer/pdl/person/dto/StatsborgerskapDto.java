package no.nav.sosialhjelp.soknad.consumer.pdl.person.dto;

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
