package no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo;

import no.aetat.arena.fodselsnr.Fodselsnr;
import no.aetat.arena.personstatus.Personstatus;
import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PersonInfoService {

    static final String UKJENT = "UKJENT";
    static final String IKKE_REGISTRERT = "IKKE_REGISTRERT";

    @Inject
    private PersonInfoServiceSoap personInfoEndpoint;
    private static final Logger logger = getLogger(PersonInfoService.class);

    public String hentArbeidssokerStatus(String fnr) {
        try {
            Personstatus personstatus = personInfoEndpoint.hentPersonStatus(new Fodselsnr().withFodselsnummer(fnr));
            return mapTilArbeidssokerstatus(personstatus);
        } catch (Exception e) {
            logger.error("Feil ved henting av personstatus for fnr {}", fnr, e);
            return UKJENT;
        }
    }

    public String hentYtelseStatus(String fnr) {
        try {
            Personstatus personstatus = personInfoEndpoint.hentPersonStatus(new Fodselsnr().withFodselsnummer(fnr));
            return mapTilYtelsesstatus(personstatus);
        } catch (Exception e) {
            logger.error("Feil ved henting av personstatus for fnr {}", fnr, e);
            return UKJENT;
        }
    }

    private static String mapTilArbeidssokerstatus(Personstatus personstatus) {
        if (personstatus == null || personstatus.getPersonData() == null) {
            return IKKE_REGISTRERT;
        }
        return personstatus.getPersonData().getStatusArbeidsoker();
    }

    private static String mapTilYtelsesstatus(Personstatus personstatus) {
        if (personstatus == null || personstatus.getPersonData() == null) {
            return IKKE_REGISTRERT;
        }
        return personstatus.getPersonData().getStatusYtelse();
    }
}
