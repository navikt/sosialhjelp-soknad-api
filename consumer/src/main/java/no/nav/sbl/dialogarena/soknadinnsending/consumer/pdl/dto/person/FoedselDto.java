package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import java.time.LocalDate;

public class FoedselDto {

    private final Integer foedselsaar;
    private final LocalDate foedselsdato;

    public FoedselDto(Integer foedselsaar, LocalDate foedselsdato) {
        this.foedselsaar = foedselsaar;
        this.foedselsdato = foedselsdato;
    }

    public Integer getFoedselsaar() {
        return foedselsaar;
    }

    public LocalDate getFoedselsdato() {
        return foedselsdato;
    }
}
