package no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo;

import no.aetat.arena.fodselsnr.Fodselsnr;
import no.aetat.arena.personstatus.Personstatus;
import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PersonInfoConnector {

    public enum Status {REGISTRERT, IKKE_REGISTRERT, UKJENT}

    @Inject
    private PersonInfoServiceSoap service;
    private static final Logger logger = getLogger(PersonInfoConnector.class);

    public Status hent(String fnr) {
        try {
            Personstatus personstatus = service.hentPersonStatus(new Fodselsnr().withFodselsnummer(fnr));
            return mapTilStatus(personstatus);
        } catch (Exception e) {
            logger.error("Feil ved henting av personstatus", e);
            return Status.UKJENT;
        }
    }

    private static Status mapTilStatus(Personstatus personstatus) {
        if (personstatus == null || personstatus.getPersonData() == null) {
            return Status.IKKE_REGISTRERT;
        }
        String statusArbeidsoker = personstatus.getPersonData().getStatusArbeidsoker();
        switch (statusArbeidsoker) {
            case "ARBS":
                return Status.REGISTRERT;
            default:
                return Status.IKKE_REGISTRERT;
        }
    }
}
