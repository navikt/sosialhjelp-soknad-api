package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDate;

public class FoedselDto {

    private final LocalDate foedselsdato;

    @JsonCreator
    public FoedselDto(LocalDate foedselsdato) {
        this.foedselsdato = foedselsdato;
    }

    public LocalDate getFoedselsdato() {
        return foedselsdato;
    }
}
