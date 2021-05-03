package no.nav.sosialhjelp.soknad.oppslag.utbetaling;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UtbetalingerResponseDto {

    private final List<UtbetalingDto> utbetalinger;
    private final boolean feilet;

    @JsonCreator
    public UtbetalingerResponseDto(
            @JsonProperty("utbetalinger") List<UtbetalingDto> utbetalinger,
            @JsonProperty("feilet") boolean feilet
    ) {
        this.utbetalinger = utbetalinger;
        this.feilet = feilet;
    }

    public List<UtbetalingDto> getUtbetalinger() {
        return utbetalinger;
    }

    public boolean getFeilet() {
        return feilet;
    }
}
