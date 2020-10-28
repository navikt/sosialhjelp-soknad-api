package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.annotation.JsonCreator;

public class AdressebeskyttelseDto {

    private final Gradering gradering;

    @JsonCreator
    public AdressebeskyttelseDto(Gradering gradering) {
        this.gradering = gradering;
    }

    public Gradering getGradering() {
        return gradering;
    }

    public enum Gradering {
        STRENGT_FORTROLIG_UTLAND, // kode 6 utland
        STRENGT_FORTROLIG, // kode 6
        FORTROLIG, // kode 7
        UGRADERT
    }
}
