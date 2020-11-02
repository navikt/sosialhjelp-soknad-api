package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PdlHentPerson<T> {

    private final T hentPerson;

    @JsonCreator
    public PdlHentPerson(
            @JsonProperty("hentPerson") T hentPerson
    ) {
        this.hentPerson = hentPerson;
    }

    public T getHentPerson() {
        return hentPerson;
    }
}
