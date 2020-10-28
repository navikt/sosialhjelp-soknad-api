package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.annotation.JsonCreator;

public class StatsborgerskapDto {

    private final String land;

    @JsonCreator
    public StatsborgerskapDto(String land) {
        this.land = land;
    }

    public String getLand() {
        return land;
    }
}
