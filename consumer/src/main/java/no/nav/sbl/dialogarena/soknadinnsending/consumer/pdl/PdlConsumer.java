package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.gt.dto.GeografiskTilknytningDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.person.PdlBarn;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.person.PdlEktefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.person.PdlPerson;

public interface PdlConsumer {

    PdlPerson hentPerson(String ident);

    PdlBarn hentBarn(String ident);

    PdlEktefelle hentEktefelle(String ident);

    void ping();

    GeografiskTilknytningDto hentGeografiskTilknytning(String ident);
}
