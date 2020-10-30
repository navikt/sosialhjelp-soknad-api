package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PdlEktefelleData {

    private final PdlEktefelle hentPerson;

    @JsonCreator
    public PdlEktefelleData(
            @JsonProperty("hentPerson") PdlEktefelle hentPerson
    ) {
        this.hentPerson = hentPerson;
    }

    public PdlEktefelle getHentPerson() {
        return hentPerson;
    }

}
