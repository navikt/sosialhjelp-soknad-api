package no.nav.sosialhjelp.soknad.web.rest.ressurser.dialog.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SistInnsendteSoknadDto {

    private final String ident;
    private final String navEnhet;
    private final LocalDateTime innsendtDato;

    public SistInnsendteSoknadDto(
            String ident,
            String navEnhet,
            LocalDateTime innsendtDato
    ) {
        this.ident = ident;
        this.navEnhet = navEnhet;
        this.innsendtDato = innsendtDato;
    }

    public String getIdent() {
        return ident;
    }

    public String getNavEnhet() {
        return navEnhet;
    }

    public String getInnsendtDato() {
        return innsendtDato.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
