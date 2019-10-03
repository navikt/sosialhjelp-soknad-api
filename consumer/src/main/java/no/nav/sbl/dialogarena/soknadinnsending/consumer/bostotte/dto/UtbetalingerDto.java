package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UtbetalingerDto {
    public LocalDate utbetalingsdato;
    public BigDecimal belop;
    public BostotteMottaker mottaker;
    public BostotteRolle rolle;

    public LocalDate getUtbetalingsdato() {
        return utbetalingsdato;
    }

    public BigDecimal getBelop() {
        return belop;
    }

    public BostotteMottaker getMottaker() {
        return mottaker;
    }

    public BostotteRolle getRolle() {
        return rolle;
    }

    public void setUtbetalingsdato(String datoString) {
        this.utbetalingsdato = LocalDate.parse(datoString);
    }

    public UtbetalingerDto with(BostotteMottaker mottaker, BigDecimal belop, LocalDate utbetalingsdato, BostotteRolle rolle) {
        this.mottaker = mottaker;
        this.belop = belop;
        this.utbetalingsdato = utbetalingsdato;
        this.rolle = rolle;
        return this;
    }
}
