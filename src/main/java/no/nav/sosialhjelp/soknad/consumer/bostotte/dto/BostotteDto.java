package no.nav.sosialhjelp.soknad.consumer.bostotte.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BostotteDto {
    public List<SakerDto> saker = new ArrayList<>();
    public List<UtbetalingerDto> utbetalinger = new ArrayList<>();

    public List<SakerDto> getSaker() {
        return saker;
    }

    public List<UtbetalingerDto> getUtbetalinger() {
        return utbetalinger;
    }

    public BostotteDto withUtbetaling(UtbetalingerDto utbetalingerDto) {
        utbetalinger.add(utbetalingerDto);
        return this;
    }

    public BostotteDto withSak(SakerDto sakerDto) {
        saker.add(sakerDto);
        return this;
    }
}
