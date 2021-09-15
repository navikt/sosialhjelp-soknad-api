package no.nav.sosialhjelp.soknad.web.rest.ressurser.dialog.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SistInnsendteSoknadDto {

    private final String ident;
    private final String enhetsnr;
    private final LocalDateTime innsendtDato;

    public SistInnsendteSoknadDto(
            String ident,
            String enhetsnr,
            LocalDateTime innsendtDato
    ) {
        this.ident = ident;
        this.enhetsnr = enhetsnr;
        this.innsendtDato = innsendtDato;
    }

    public String getIdent() {
        return ident;
    }

    public String getEnhetsnr() {
        return enhetsnr;
    }

    public String getInnsendtDato() {
        return innsendtDato.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
