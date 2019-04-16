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
        JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        String personIdentifikator = personalia.getPersonIdentifikator().getVerdi();
        JsonPersonalia systemPersonalia = innhentSystemBasisPersonalia(personIdentifikator);

        if (systemPersonalia == null){
            return;
        }

        personalia.setPersonIdentifikator(systemPersonalia.getPersonIdentifikator());
        personalia.setNavn(systemPersonalia.getNavn());
        personalia.setStatsborgerskap(systemPersonalia.getStatsborgerskap());
        personalia.setNordiskBorger(systemPersonalia.getNordiskBorger());
    }

    public JsonPersonalia innhentSystemBasisPersonalia(String personIdentifikator) {
        Personalia personalia = personaliaFletter.mapTilPersonalia(personIdentifikator);
        if (personalia == null){
            return null;
        }

        return mapToJsonPersonalia(personalia);
    }

    private JsonPersonalia mapToJsonPersonalia(Personalia personalia){
        JsonPersonalia jsonPersonalia = new JsonPersonalia()
                .withPersonIdentifikator(new JsonPersonIdentifikator()
                        .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                        .withVerdi(personalia.getFnr()))
                .withNavn(new JsonSokernavn()
                        .withKilde(JsonSokernavn.Kilde.SYSTEM)
                        .withFornavn(personalia.getFornavn())
                        .withMellomnavn(personalia.getMellomnavn())
                        .withEtternavn(personalia.getEtternavn()));

        setNavnPaaJsonPersonalia(jsonPersonalia, personalia);
        setStatsborgerskapPaaJsonPersonalia(jsonPersonalia, personalia);
        setNordiskBorgerPaaPersonalia(jsonPersonalia, personalia);

        return jsonPersonalia;
    }

    private void setNordiskBorgerPaaPersonalia(JsonPersonalia jsonPersonalia, Personalia personalia) {
        Boolean nordiskBorger = erNordiskBorger(personalia.getStatsborgerskap());
        if (nordiskBorger == null){
            return;
        }
        if (jsonPersonalia.getNordiskBorger() == null){
            jsonPersonalia.setNordiskBorger(new JsonNordiskBorger()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(nordiskBorger));
        } else {
            jsonPersonalia.getNordiskBorger().setKilde(JsonKilde.SYSTEM);
            jsonPersonalia.getNordiskBorger().setVerdi(nordiskBorger);
        }
    }

    private void setStatsborgerskapPaaJsonPersonalia(JsonPersonalia jsonPersonalia, Personalia personalia) {
        if (personalia.getStatsborgerskap() == null){
            return;
        }
        if (jsonPersonalia.getStatsborgerskap() == null){
            jsonPersonalia.setStatsborgerskap(new JsonStatsborgerskap()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(personalia.getStatsborgerskap()));
        } else {
            jsonPersonalia.getStatsborgerskap().setKilde(JsonKilde.SYSTEM);
            jsonPersonalia.getStatsborgerskap().setVerdi(personalia.getStatsborgerskap());
        }
    }

    private void setNavnPaaJsonPersonalia(JsonPersonalia jsonPersonalia, Personalia personalia) {
        jsonPersonalia.getNavn().setFornavn(personalia.getFornavn() != null ? personalia.getFornavn() : "");
        jsonPersonalia.getNavn().setMellomnavn(personalia.getMellomnavn() != null ? personalia.getMellomnavn() : "");
        jsonPersonalia.getNavn().setEtternavn(personalia.getEtternavn() != null ? personalia.getEtternavn() : "");
    }

    private static Boolean erNordiskBorger(String statsborgerskap) {
        if (statsborgerskap == null){
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
