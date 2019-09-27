package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto;

import java.util.ArrayList;
import java.util.List;

public class BostotteDto {
    public List<SakerDto> saker = new ArrayList<>();
    public List<UtbetalingerDto> utbetalinger= new ArrayList<>();

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
