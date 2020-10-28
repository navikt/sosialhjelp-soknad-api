package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;

public class PdlData {

    private final PdlPerson hentPerson;

    @JsonCreator
    public PdlData(
            @JsonProperty("hentPerson") PdlPerson hentPerson
    ) {
        this.hentPerson = hentPerson;
    }

    public PdlPerson getHentPerson() {
        return hentPerson;
    }

}
