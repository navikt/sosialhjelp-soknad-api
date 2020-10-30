package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PdlPersonData {

    private final PdlPerson hentPerson;

    @JsonCreator
    public PdlPersonData(
            @JsonProperty("hentPerson") PdlPerson hentPerson
    ) {
        this.hentPerson = hentPerson;
    }

    public PdlPerson getHentPerson() {
        return hentPerson;
    }

}
