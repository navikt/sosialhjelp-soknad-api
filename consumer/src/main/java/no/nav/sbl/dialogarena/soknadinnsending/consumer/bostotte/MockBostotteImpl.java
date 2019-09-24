package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.SakerDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.UtbetalingerDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.VedtakDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MockBostotteImpl implements Bostotte {

    @Override
    public BostotteDto hentBostotte(String personIdentifikator, LocalDate fra, LocalDate til) {
        String mottaker = "KOMMUNE";
        BigDecimal belop = BigDecimal.valueOf(10000);
        LocalDate utbetalingsDato = LocalDate.now();
        LocalDate saksDato = LocalDate.now().minusDays(3);
        String saksStatus = "VEDTATT";
        String rolle = "HOVEDPERSON";
        String vedtaksKode = "V03";
        String vedtaksBeskrivelse = "Avslag - For høy inntekt";

        UtbetalingerDto utbetalingerDto = new UtbetalingerDto()
                .with(mottaker, belop, utbetalingsDato);
        VedtakDto vedtakDto = new VedtakDto()
                .with(vedtaksKode, vedtaksBeskrivelse);
        SakerDto sakerDto = new SakerDto()
                .with(saksDato.getMonthValue(), saksDato.getYear(), saksStatus, vedtakDto, rolle);

        return new BostotteDto()
                .withUtbetaling(utbetalingerDto)
                .withSak(sakerDto);
    }
}
