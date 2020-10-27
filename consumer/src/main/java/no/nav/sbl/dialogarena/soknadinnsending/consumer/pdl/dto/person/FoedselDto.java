package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import java.time.LocalDate;

public class FoedselDto {

    private final LocalDate foedselsdato;

    public FoedselDto(LocalDate foedselsdato) {
        this.foedselsdato = foedselsdato;
    }

    public LocalDate getFoedselsdato() {
        return foedselsdato;
    }
}
