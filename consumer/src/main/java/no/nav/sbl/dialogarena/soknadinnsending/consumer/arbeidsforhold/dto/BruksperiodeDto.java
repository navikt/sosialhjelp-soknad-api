package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BruksperiodeDto {

    private LocalDateTime fom;
    private LocalDateTime tom;

    public BruksperiodeDto(LocalDateTime fom, LocalDateTime tom) {
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDateTime getFom() {
        return fom;
    }

    public LocalDateTime getTom() {
        return tom;
    }
}
