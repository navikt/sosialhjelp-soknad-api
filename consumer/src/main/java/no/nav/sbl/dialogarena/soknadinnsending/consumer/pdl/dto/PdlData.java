package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;

public class PdlData {

    private final PdlPerson hentPerson;

    public PdlData(PdlPerson hentPerson) {
        this.hentPerson = hentPerson;
    }

    public PdlPerson getHentPerson() {
        return hentPerson;
    }

}
