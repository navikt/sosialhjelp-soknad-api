package no.nav.sbl.dialogarena.sendsoknad.mockmodul.person;

import no.nav.tjeneste.virksomhet.person.v1.*;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;

import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock.*;

public class PersonPortTypeMock implements PersonPortType {
    Person person;
    Person barn;
    Person barn2;
    Person barn3;
    Person ektefelle;

    @Override
    public HentKjerneinformasjonResponse hentKjerneinformasjon(HentKjerneinformasjonRequest request) throws HentKjerneinformasjonPersonIkkeFunnet, HentKjerneinformasjonSikkerhetsbegrensning {
        HentKjerneinformasjonResponse respons = new HentKjerneinformasjonResponse();

        if (FNR_EKTEFELLE.equals(request.getIdent())) {
            respons.setPerson(ektefelle);
        } else if (FNR_BARN.equals(request.getIdent())) {
            respons.setPerson(barn);
        } else if (FNR_BARN2.equals(request.getIdent())) {
            respons.setPerson(barn2);
        } else if (FNR_BARN3.equals(respons.getPerson())) {
            respons.setPerson(barn3);
        } else {
            respons.setPerson(person);
        }
        return respons;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setBarn(Person barn) {
        this.barn = barn;
    }

    public void setBarn2(Person barn2) {
        this.barn2 = barn2;
    }

    public void setBarn3(Person barn3) {
        this.barn3 = barn3;
    }

    public void setEktefelle(Person ektefelle) {
        this.ektefelle = ektefelle;
    }

    public Person getPerson(){
        return person;
    }

    @Override
    public void ping() {

    }
}
