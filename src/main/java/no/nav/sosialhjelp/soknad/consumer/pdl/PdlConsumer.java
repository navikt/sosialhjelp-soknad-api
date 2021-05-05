package no.nav.sosialhjelp.soknad.consumer.pdl;

import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.dto.AdresseSokResult;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlAdressebeskyttelse;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlBarn;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlEktefelle;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPerson;

import java.util.Map;

public interface PdlConsumer {

    PdlPerson hentPerson(String ident);

    PdlBarn hentBarn(String ident);

    PdlEktefelle hentEktefelle(String ident);

    PdlAdressebeskyttelse hentAdressebeskyttelse(String ident);

    void ping();

    AdresseSokResult getAdresseSokResult(Map<String, Object> variables);
}
