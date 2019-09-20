package no.nav.sbl.dialogarena.bostotte.dto;

import java.time.LocalDate;

public class SakerDto {
    private Integer mnd;
    private Integer ar;
    private String status;
    private VedtakDto vedtak;
    private String rolle;

    public Integer getMnd() {
        return mnd;
    }

    public Integer getAr() {
        return ar;
    }

    public String getStatus() {
        return status;
    }

    public VedtakDto getVedtak() {
        return vedtak;
    }

    public String getRolle() {
        return rolle;
    }

    public LocalDate getDato() {
        return LocalDate.of(ar, mnd, 1);
    }
}
