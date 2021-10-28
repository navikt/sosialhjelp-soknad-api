package no.nav.sosialhjelp.soknad.consumer.bostotte.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.nav.sosialhjelp.soknad.client.husbanken.enums.BostotteRolle;
import no.nav.sosialhjelp.soknad.client.husbanken.enums.BostotteStatus;

import java.time.LocalDate;

@SuppressWarnings("WeakerAccess")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SakerDto {
    public Integer mnd;
    public Integer ar;
    public BostotteStatus status;
    public VedtakDto vedtak;
    public BostotteRolle rolle;

    public BostotteStatus getStatus() {
        return status;
    }

    public VedtakDto getVedtak() {
        return vedtak;
    }

    public LocalDate getDato() {
        return LocalDate.of(ar, mnd, 1);
    }

    public BostotteRolle getRolle() {
        return rolle;
    }

    public SakerDto with(Integer mnd, Integer ar, BostotteStatus status, VedtakDto vedtak, BostotteRolle rolle) {
        this.mnd = mnd;
        this.ar = ar;
        this.status = status;
        this.vedtak = vedtak;
        this.rolle = rolle;
        return this;
    }
}
