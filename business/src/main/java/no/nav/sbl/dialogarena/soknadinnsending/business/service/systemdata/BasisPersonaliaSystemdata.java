package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.PdlApiException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdlperson.PersonSammenligner;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class BasisPersonaliaSystemdata implements Systemdata {

    private static final Logger log = getLogger(BasisPersonaliaSystemdata.class);

    @Inject
    private PersonService personService;

    @Inject
    private PdlService pdlService;

    @Inject
    PersonSammenligner personSammenligner;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        final JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        final String personIdentifikator = personalia.getPersonIdentifikator().getVerdi();
        final JsonPersonalia systemPersonalia = innhentSystemBasisPersonalia(personIdentifikator);

        if (systemPersonalia == null){
            return;
        }

        personalia.setNavn(systemPersonalia.getNavn());
        personalia.setStatsborgerskap(systemPersonalia.getStatsborgerskap());
        personalia.setNordiskBorger(systemPersonalia.getNordiskBorger());
    }

    public JsonPersonalia innhentSystemBasisPersonalia(final String personIdentifikator) {
        Person person = personService.hentPerson(personIdentifikator);
        if (person == null){
            return null;
        }
        try {
            Person pdlPerson = pdlService.hentPerson(personIdentifikator);
            if (pdlPerson != null) {
                personSammenligner.sammenlign(person, pdlPerson);
            }
        } catch (PdlApiException | TjenesteUtilgjengeligException e) {
            log.warn("PDL kaster feil (brukes kun for sammenligning)", e);
        } catch (Exception e) {
            log.warn("PDL-feil eller feil ved sammenligning av data fra TPS/PDL", e);
        }
        return mapToJsonPersonalia(person);
    }

    private JsonPersonalia mapToJsonPersonalia(Person person){
        return new JsonPersonalia()
                .withPersonIdentifikator(mapToJsonPersonIdentifikator(person))
                .withNavn(mapToJsonSokernavn(person))
                .withStatsborgerskap(mapToJsonStatsborgerskap(person))
                .withNordiskBorger(mapToJsonNordiskBorger(person));
    }

    private JsonPersonIdentifikator mapToJsonPersonIdentifikator(Person person) {
        return new JsonPersonIdentifikator()
                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                .withVerdi(person.getFnr());
    }

    private JsonSokernavn mapToJsonSokernavn(Person person) {
        return new JsonSokernavn()
                .withKilde(JsonSokernavn.Kilde.SYSTEM)
                .withFornavn(person.getFornavn() != null ? person.getFornavn() : "")
                .withMellomnavn(person.getMellomnavn() != null ? person.getMellomnavn() : "")
                .withEtternavn(person.getEtternavn() != null ? person.getEtternavn() : "");
    }

    private JsonStatsborgerskap mapToJsonStatsborgerskap(Person person) {
        String statsborgerskap = person.getStatsborgerskap();
        if (statsborgerskap == null || statsborgerskap.equals("???")){
            return null;
        }

        return new JsonStatsborgerskap()
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(statsborgerskap);
    }

    private JsonNordiskBorger mapToJsonNordiskBorger(Person person) {
        Boolean nordiskBorger = erNordiskBorger(person.getStatsborgerskap());
        if (nordiskBorger == null){
            return null;
        }
        return new JsonNordiskBorger()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(nordiskBorger);
    }

    static Boolean erNordiskBorger(String statsborgerskap) {
        if (statsborgerskap == null || statsborgerskap.equals("???")){
            return null;
        }
        switch (statsborgerskap) {
            case "NOR":
            case "SWE":
            case "FRO":
            case "ISL":
            case "DNK":
            case "FIN":
                return true;
            default:
                return false;
        }
    }
}
