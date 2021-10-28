package no.nav.sosialhjelp.soknad.consumer.bostotte.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.nav.sosialhjelp.soknad.client.husbanken.enums.BostotteMottaker;
import no.nav.sosialhjelp.soknad.client.husbanken.enums.BostotteRolle;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UtbetalingerDto {
    public LocalDate utbetalingsdato;
    public BigDecimal belop;
    public BostotteMottaker mottaker;
    @SuppressWarnings("WeakerAccess")
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
