package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class BasisPersonaliaSystemdata implements Systemdata {

    @Inject
    private PersonaliaFletter personaliaFletter;


    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
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
        final Personalia personalia = personaliaFletter.mapTilPersonalia(personIdentifikator);
        if (personalia == null){
            return null;
        }

        return mapToJsonPersonalia(personalia);
    }

    private JsonPersonalia mapToJsonPersonalia(Personalia personalia){
        return new JsonPersonalia()
                .withPersonIdentifikator(mapToJsonPersonIdentifikator(personalia))
                .withNavn(mapToJsonSokernavn(personalia))
                .withStatsborgerskap(mapToJsonStatsborgerskap(personalia))
                .withNordiskBorger(mapToJsonNordiskBorger(personalia));
    }

    private JsonPersonIdentifikator mapToJsonPersonIdentifikator(Personalia personalia) {
        return new JsonPersonIdentifikator()
                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                .withVerdi(personalia.getFnr());
    }

    private JsonSokernavn mapToJsonSokernavn(Personalia personalia) {
        return new JsonSokernavn()
                .withKilde(JsonSokernavn.Kilde.SYSTEM)
                .withFornavn(personalia.getFornavn() != null ? personalia.getFornavn() : "")
                .withMellomnavn(personalia.getMellomnavn() != null ? personalia.getMellomnavn() : "")
                .withEtternavn(personalia.getEtternavn() != null ? personalia.getEtternavn() : "");
    }

    private JsonStatsborgerskap mapToJsonStatsborgerskap(Personalia personalia) {
        String statsborgerskap = personalia.getStatsborgerskap();
        if (statsborgerskap == null || statsborgerskap.equals("???")){
            return null;
        }

        return new JsonStatsborgerskap()
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(statsborgerskap);
    }

    private JsonNordiskBorger mapToJsonNordiskBorger(Personalia personalia) {
        Boolean nordiskBorger = erNordiskBorger(personalia.getStatsborgerskap());
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
