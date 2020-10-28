package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;

public class PdlData {

    private final PdlPerson hentPerson;

    @JsonCreator
    public PdlData(PdlPerson hentPerson) {
        this.hentPerson = hentPerson;
    }

    public PdlPerson getHentPerson() {
        return hentPerson;
    }

}
