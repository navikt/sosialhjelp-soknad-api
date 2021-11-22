package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonNordiskBorger;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.person.PersonService;
import no.nav.sosialhjelp.soknad.person.domain.Person;
import org.springframework.stereotype.Component;

@Component
public class BasisPersonaliaSystemdata implements Systemdata {

    private static final String NOR = "NOR";
    private static final String SWE = "SWE";
    private static final String FRO = "FRO";
    private static final String ISL = "ISL";
    private static final String DNK = "DNK";
    private static final String FIN = "FIN";
    public static final String PDL_UKJENT_STATSBORGERSKAP = "XUK";
    public static final String PDL_STATSLOS = "XXX";

    private final PersonService personService;

    public BasisPersonaliaSystemdata(PersonService personService) {
        this.personService = personService;
    }

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
        var person = personService.hentPerson(personIdentifikator);
        if (person == null){
            return null;
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
        String statsborgerskap = prioritertStatsborgerskap(person);
        if (statsborgerskap == null || statsborgerskap.equals("???") || statsborgerskap.equals(PDL_UKJENT_STATSBORGERSKAP) || statsborgerskap.equals(PDL_STATSLOS)){
            return null;
        }

        return new JsonStatsborgerskap()
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(statsborgerskap);
    }

    private JsonNordiskBorger mapToJsonNordiskBorger(Person person) {
        Boolean nordiskBorger = erNordiskBorger(prioritertStatsborgerskap(person));
        if (nordiskBorger == null){
            return null;
        }
        return new JsonNordiskBorger()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(nordiskBorger);
    }

    static Boolean erNordiskBorger(String statsborgerskap) {
        if (statsborgerskap == null || statsborgerskap.equals("???") || statsborgerskap.equals(PDL_UKJENT_STATSBORGERSKAP) || statsborgerskap.equals(PDL_STATSLOS)){
            return null;
        }
        switch (statsborgerskap) {
            case NOR:
            case SWE:
            case FRO:
            case ISL:
            case DNK:
            case FIN:
                return true;
            default:
                return false;
        }
    }

    private String prioritertStatsborgerskap(Person person) {
        var list = person.getStatsborgerskap();

        if (list.isEmpty()) {
            return null;
        }
        if (list.contains(NOR)) {
            return NOR;
        }
        if (list.contains(SWE)) {
            return SWE;
        }
        if (list.contains(FRO)) {
            return FRO;
        }
        if (list.contains(ISL)) {
            return ISL;
        }
        if (list.contains(DNK)) {
            return DNK;
        }
        if (list.contains(FIN)) {
            return FIN;
        }

        return list.get(0);
    }
}
