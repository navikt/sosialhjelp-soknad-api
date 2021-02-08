package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.person;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HentPerson<T> {

    private final T hentPerson;

    @JsonCreator
    public HentPerson(
            @JsonProperty("hentPerson") T hentPerson
    ) {
        this.hentPerson = hentPerson;
    }

    public T getHentPerson() {
        return hentPerson;
    }
}
