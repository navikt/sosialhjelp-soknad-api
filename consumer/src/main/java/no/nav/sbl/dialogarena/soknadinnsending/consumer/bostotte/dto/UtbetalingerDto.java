package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UtbetalingerDto {
    public LocalDate utbetalingsdato;
    public BigDecimal belop;
    public String mottaker;
    public String rolle;

    public LocalDate getUtbetalingsdato() {
        return utbetalingsdato;
    }

    public BigDecimal getBelop() {
        return belop;
    }

    public String getMottaker() {
        return mottaker;
    }

    public String getRolle() {
        return rolle;
    }

    public UtbetalingerDto with(String mottaker, BigDecimal belop, LocalDate utbetalingsdato) {
        this.mottaker = mottaker;
        this.belop = belop;
        this.utbetalingsdato = utbetalingsdato;
        return this;
    }
}
