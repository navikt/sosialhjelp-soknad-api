package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class FoedselDto {

    private final LocalDate foedselsdato;

    @JsonCreator
    public FoedselDto(
            @JsonProperty("foedselsdato") LocalDate foedselsdato
    ) {
        this.foedselsdato = foedselsdato;
    }

    public LocalDate getFoedselsdato() {
        return foedselsdato;
    }
}
