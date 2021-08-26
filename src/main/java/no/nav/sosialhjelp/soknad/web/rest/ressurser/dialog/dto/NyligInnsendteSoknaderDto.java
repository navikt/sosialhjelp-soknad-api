package no.nav.sosialhjelp.soknad.web.rest.ressurser.dialog.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NyligInnsendteSoknaderDto {

    private final String digisosId;
    private final String enhetsnr;
    private final LocalDateTime innsendtDato;

    public NyligInnsendteSoknaderDto(
            String digisosId,
            String enhetsnr,
            LocalDateTime innsendtDato
    ) {
        this.digisosId = digisosId;
        this.enhetsnr = enhetsnr;
        this.innsendtDato = innsendtDato;
    }

    public String getDigisosId() {
        return digisosId;
    }

    public String getEnhetsnr() {
        return enhetsnr;
    }

    public String getInnsendtDato() {
        return innsendtDato.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
