package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PdlBarnData {

    private final PdlBarn hentPerson;

    @JsonCreator
    public PdlBarnData(
            @JsonProperty("hentPerson") PdlBarn hentPerson
    ) {
        this.hentPerson = hentPerson;
    }

    public PdlBarn getHentPerson() {
        return hentPerson;
    }

}
