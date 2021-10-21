package no.nav.sosialhjelp.soknad.consumer.pdl.person;

public interface PdlHentPersonConsumer {

    PdlPerson hentPerson(String ident);

    PdlBarn hentBarn(String ident, int index);

    PdlEktefelle hentEktefelle(String ident);

    PdlAdressebeskyttelse hentAdressebeskyttelse(String ident);

    void ping();
}
