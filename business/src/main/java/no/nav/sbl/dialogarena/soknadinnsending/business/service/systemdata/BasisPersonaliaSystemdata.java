package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.PersonServiceV3;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.PersonData;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class BasisPersonaliaSystemdata implements Systemdata {

    @Inject
    private PersonServiceV3 personService;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        final JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        final String personIdentifikator = personalia.getPersonIdentifikator().getVerdi();
        final JsonPersonalia systemPersonalia = innhentSystemBasisPersonalia(personIdentifikator);

        if (systemPersonalia == null) {
            return;
        }

        personalia.setNavn(systemPersonalia.getNavn());
        personalia.setStatsborgerskap(systemPersonalia.getStatsborgerskap());
        personalia.setNordiskBorger(systemPersonalia.getNordiskBorger());
    }

    public JsonPersonalia innhentSystemBasisPersonalia(final String personIdentifikator) {
        PersonData personData = (personService.getPersonData(personIdentifikator));
        if (personData == null) {
            return null;
        }

        return mapToJsonPersonalia(personData);
    }

    private JsonPersonalia mapToJsonPersonalia(PersonData person) {
        return new JsonPersonalia()
                .withPersonIdentifikator(mapToJsonPersonIdentifikator(person))
                .withNavn(mapToJsonSokernavn(person))
                .withStatsborgerskap(mapToJsonStatsborgerskap(person))
                .withNordiskBorger(mapToJsonNordiskBorger(person));
    }

    private JsonPersonIdentifikator mapToJsonPersonIdentifikator(PersonData person) {
        return new JsonPersonIdentifikator()
                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                .withVerdi(person.getFodselsnummer());
    }

    private JsonSokernavn mapToJsonSokernavn(PersonData person) {
        return new JsonSokernavn()
                .withKilde(JsonSokernavn.Kilde.SYSTEM)
                .withFornavn(person.getFornavn() != null ? person.getFornavn() : "")
                .withMellomnavn(person.getMellomnavn() != null ? person.getMellomnavn() : "")
                .withEtternavn(person.getEtternavn() != null ? person.getEtternavn() : "");
    }

    private JsonStatsborgerskap mapToJsonStatsborgerskap(PersonData person) {
        String statsborgerskap = person.getStatsborgerskap();
        if (statsborgerskap == null || statsborgerskap.equals("???")) {
            return null;
        }

        return new JsonStatsborgerskap()
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(statsborgerskap);
    }

    private JsonNordiskBorger mapToJsonNordiskBorger(PersonData person) {
        Boolean nordiskBorger = erNordiskBorger(person.getStatsborgerskap());
        if (nordiskBorger == null) {
            return null;
        }
        return new JsonNordiskBorger()
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(nordiskBorger);
    }

    static Boolean erNordiskBorger(String statsborgerskap) {
        if (statsborgerskap == null || statsborgerskap.equals("???")) {
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
