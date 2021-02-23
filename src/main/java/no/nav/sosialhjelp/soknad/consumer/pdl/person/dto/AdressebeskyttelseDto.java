package no.nav.sosialhjelp.soknad.consumer.pdl.person.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AdressebeskyttelseDto {

    private final Gradering gradering;

    @JsonCreator
    public AdressebeskyttelseDto(
            @JsonProperty("gradering") Gradering gradering
    ) {
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
