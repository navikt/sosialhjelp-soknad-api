package no.nav.sosialhjelp.soknad.web.rest.ressurser.eksponerte.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InnsendtSoknadDto {

    private final String navn;
    private final String kode;
    private final LocalDateTime sistEndret;

    public InnsendtSoknadDto(
            String navn,
            String kode,
            LocalDateTime sistEndret
    ) {
        this.navn = navn;
        this.kode = kode;
        this.sistEndret = sistEndret;
    }

    public String getNavn() {
        return navn;
    }

    public String getKode() {
        return kode;
    }

    public String getSistEndret() {
        return sistEndret.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
