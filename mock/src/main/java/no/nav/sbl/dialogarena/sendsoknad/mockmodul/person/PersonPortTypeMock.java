package no.nav.sbl.dialogarena.sendsoknad.mockmodul.person;

import no.nav.tjeneste.virksomhet.person.v1.*;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;

public class PersonPortTypeMock implements PersonPortType {
    private Person person;

    @Override
    public HentKjerneinformasjonResponse hentKjerneinformasjon(HentKjerneinformasjonRequest request) throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        HentKjerneinformasjonResponse respons = new HentKjerneinformasjonResponse();
        respons.setPerson(person);
        return respons;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Person getPerson(){
        return person;
    }

    @Override
    public void ping() {

    }
}
