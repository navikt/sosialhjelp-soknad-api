package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;

public interface PdlConsumer {

    PdlPerson hentPerson(String ident);

    PdlPerson hentBarn(String ident);

    PdlPerson hentEktefelle(String ident);

    void ping();
}
