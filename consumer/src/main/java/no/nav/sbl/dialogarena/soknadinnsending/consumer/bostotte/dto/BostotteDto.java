package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto;

import java.util.ArrayList;
import java.util.List;

public class BostotteDto {
    List<SakerDto> saker = new ArrayList<>();
    List<UtbetalingerDto> utbetalinger= new ArrayList<>();

    public List<SakerDto> getSaker() {
        return saker;
    }

    public List<UtbetalingerDto> getUtbetalinger() {
        return utbetalinger;
    }

    public BostotteDto withWithUtbetaling(UtbetalingerDto utbetalingerDto) {
        utbetalinger.add(utbetalingerDto);
        return this;
    }
}
