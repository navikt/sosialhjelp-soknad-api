package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto;

import java.time.LocalDate;

public class SakerDto {
    private Integer mnd;
    private Integer ar;
    private String status;
    private VedtakDto vedtak;
    private String rolle;

    public String getStatus() {
        return status;
    }

    public VedtakDto getVedtak() {
        return vedtak;
    }

    public LocalDate getDato() {
        return LocalDate.of(ar, mnd, 1);
    }

    public SakerDto with(Integer mnd, Integer ar, String status, VedtakDto vedtak, String rolle) {
        this.mnd = mnd;
        this.ar = ar;
        this.status = status;
        this.vedtak = vedtak;
        this.rolle = rolle;
        return this;
    }
}
