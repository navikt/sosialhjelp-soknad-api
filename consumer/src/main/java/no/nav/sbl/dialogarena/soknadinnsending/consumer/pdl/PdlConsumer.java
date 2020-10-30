package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn.PdlBarn;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle.PdlEktefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;

public interface PdlConsumer {

    PdlPerson hentPerson(String ident);

    PdlBarn hentBarn(String ident);

    PdlEktefelle hentEktefelle(String ident);

    void ping();
}
