package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.UtbetalingerDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MockBostotteImpl implements Bostotte {

    @Override
    public BostotteDto hentBostotte(String personIdentifikator, LocalDate fra, LocalDate til) {
        String mottaker = "Ola Normann";
        BigDecimal belop = BigDecimal.valueOf(10000);
        LocalDate utbetalingsDato = LocalDate.now();
        return new BostotteDto().withWithUtbetaling(new UtbetalingerDto().with(mottaker, belop, utbetalingsDato));
    }
}
