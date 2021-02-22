package no.nav.sosialhjelp.soknad.consumer.pdl;

import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlBarn;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlEktefelle;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPerson;

public interface PdlConsumer {

    PdlPerson hentPerson(String ident);

    PdlBarn hentBarn(String ident);

    PdlEktefelle hentEktefelle(String ident);

    void ping();
}
