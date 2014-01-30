package no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo;

import no.aetat.arena.fodselsnr.Fodselsnr;
import no.aetat.arena.personstatus.Personstatus;
import no.nav.arena.tjenester.person.v1.FaultGeneriskMsg;
import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;
import no.nav.modig.core.exception.ApplicationException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class PersonInfoConnector {

    @Inject
    private PersonInfoServiceSoap service;

    public Personstatus hent(String fnr) {
        try {
            return service.hentPersonStatus(new Fodselsnr().withFodselsnummer(fnr));
        } catch (FaultGeneriskMsg faultGeneriskMsg) {
            throw new ApplicationException("hentPersonStatus feilet", faultGeneriskMsg);
        }
    }
}
