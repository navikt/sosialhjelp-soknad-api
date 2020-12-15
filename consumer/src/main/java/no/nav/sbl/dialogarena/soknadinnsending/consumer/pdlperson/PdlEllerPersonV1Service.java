package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdlperson;

import no.finn.unleash.Unleash;
import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.PdlApiException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PdlEllerPersonV1Service {

    public static final String UNLEASH_BRUK_PDL = "sosialhjelp.soknad.bruk-pdl";

    private static final Logger log = getLogger(PdlEllerPersonV1Service.class);

    private final PdlService pdlService;
    private final PersonService personService;
    private final PersonSammenligner personSammenligner;
    private final Unleash unleashConsumer;

    public PdlEllerPersonV1Service(PdlService pdlService, PersonService personService, PersonSammenligner personSammenligner, Unleash unleashConsumer) {
        this.pdlService = pdlService;
        this.personService = personService;
        this.personSammenligner = personSammenligner;
        this.unleashConsumer = unleashConsumer;
    }

    public Person hentPerson(String ident) {
        var pdlEnabled = unleashConsumer.isEnabled(UNLEASH_BRUK_PDL);
        log.info("sosialhjelp.soknad.bruk-pdl: {}", pdlEnabled);
        if (pdlEnabled) {
            return hentPersonFraPdl(ident);
        }
        return hentPersonFraTps(ident);
    }

    public List<Barn> hentBarn(String ident) {
        var pdlEnabled = unleashConsumer.isEnabled(UNLEASH_BRUK_PDL);
        log.info("sosialhjelp.soknad.bruk-pdl: {}", pdlEnabled);
        if (pdlEnabled) {
            return hentBarnFraPdl(ident);
        }
        return hentBarnFraTps(ident);
    }

    private Person hentPersonFraPdl(String ident) {
        try {
            return pdlService.hentPerson(ident);
        } catch (Exception e) {
            log.warn("Noe feilet ved kall til PDL. Forsøker å bruke Person_v1 som fallback.", e);
            return personService.hentPerson(ident);
        }
    }

    private Person hentPersonFraTps(String ident) {
        var person = personService.hentPerson(ident);
        // sammenlign TPS med PDL enn så lenge TPS er foretrukket
        try {
            var pdlPerson = pdlService.hentPerson(ident);
            if (pdlPerson != null) {
                personSammenligner.sammenlign(person, pdlPerson);
            }
        } catch (PdlApiException | TjenesteUtilgjengeligException e) {
            log.warn("PDL kaster feil (brukes kun for sammenligning)", e);
        } catch (Exception e) {
            log.warn("PDL-feil eller feil ved sammenligning av data fra TPS/PDL", e);
        }

        return person;
    }

    private List<Barn> hentBarnFraPdl(String ident) {
        try {
            return pdlService.hentBarnForPerson(ident);
        } catch (Exception e) {
            log.warn("Noe feilet ved kall til PDL. Forsøker å bruke Person_v1 som fallback.", e);
            return personService.hentBarn(ident);
        }
    }

    private List<Barn> hentBarnFraTps(String ident) {
        var alleBarn = personService.hentBarn(ident);
        // sammenlign TPS med PDL enn så lenge TPS er foretrukket
        try {
            var pdlBarn = pdlService.hentBarnForPerson(ident);
            if (pdlBarn != null) {
                personSammenligner.sammenlignBarn(alleBarn, pdlBarn);
            }
        } catch (PdlApiException | TjenesteUtilgjengeligException e) {
            log.warn("PDL kaster feil (brukes kun for sammenligning)", e);
        } catch (Exception e) {
            log.warn("PDL-feil eller feil ved sammenligning av data fra TPS/PDL", e);
        }

        return alleBarn;
    }

}
