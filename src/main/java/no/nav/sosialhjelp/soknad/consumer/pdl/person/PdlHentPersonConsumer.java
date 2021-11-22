package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import no.nav.sosialhjelp.soknad.person.dto.BarnDto;
import no.nav.sosialhjelp.soknad.person.dto.EktefelleDto;
import no.nav.sosialhjelp.soknad.person.dto.PersonAdressebeskyttelseDto;
import no.nav.sosialhjelp.soknad.person.dto.PersonDto;

public interface PdlHentPersonConsumer {

    PersonDto hentPerson(String ident);

    BarnDto hentBarn(String ident);

    EktefelleDto hentEktefelle(String ident);

    PersonAdressebeskyttelseDto hentAdressebeskyttelse(String ident);

    void ping();
}
