package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
public class FamilieSystemdata implements Systemdata {

    @Inject
    private PersonaliaFletter personaliaFletter;

    @Inject
    private PersonService personService;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        JsonData jsonData = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData();
        String personIdentifikator = jsonData.getPersonalia().getPersonIdentifikator().getVerdi();
        JsonFamilie familie = jsonData.getFamilie();
        if (familie.getSivilstatus() == null || familie.getSivilstatus().getKilde() == JsonKilde.SYSTEM){
            JsonSivilstatus systemverdiSivilstatus = innhentSystemverdiSivilstatus(personIdentifikator);
            if (systemverdiSivilstatus != null){
                familie.setSivilstatus(systemverdiSivilstatus);
            }
        }
        JsonHarForsorgerplikt harForsorgerplikt = familie.getForsorgerplikt().getHarForsorgerplikt();
        if (harForsorgerplikt == null || harForsorgerplikt.getKilde() == null ||
                harForsorgerplikt.getKilde() == JsonKilde.SYSTEM){
            JsonForsorgerplikt systemverdiForsorgerplikt = innhentSystemverdiForsorgerplikt(personIdentifikator);
            if(systemverdiForsorgerplikt != null){
                familie.setForsorgerplikt(systemverdiForsorgerplikt);
            }
        }
    }

    private JsonSivilstatus innhentSystemverdiSivilstatus(String personIdentifikator) {
        Personalia personalia = personaliaFletter.mapTilPersonalia(personIdentifikator);
        if (!isNotEmpty(personalia.getSivilstatus())) {
            return null;
        }
        JsonSivilstatus jsonSivilstatus = new JsonSivilstatus()
                .withKilde(JsonKilde.SYSTEM)
                .withStatus(JsonSivilstatus.Status.fromValue(personalia.getSivilstatus()));

        Ektefelle ektefelle = personalia.getEktefelle();
        if (ektefelle != null) {
            if (jsonSivilstatus.getEktefelle() == null){
                jsonSivilstatus.setEktefelle(new JsonEktefelle()
                        .withNavn(new JsonNavn()));
            }
            jsonSivilstatus.getEktefelle().getNavn()
                    .withFornavn(ektefelle.getFornavn())
                    .withMellomnavn(ektefelle.getMellomnavn())
                    .withEtternavn(ektefelle.getEtternavn());
            jsonSivilstatus.getEktefelle()
                    .withFodselsdato(ektefelle.getFodselsdato() != null ? ektefelle.getFodselsdato().toString() : null)
                    .withPersonIdentifikator(ektefelle.getFnr());
            jsonSivilstatus
                    .withEktefelleHarDiskresjonskode(ektefelle.harIkketilgangtilektefelle())
                    .withFolkeregistrertMedEktefelle(ektefelle.erFolkeregistrertsammen());
        }
        return jsonSivilstatus;
    }

    private JsonForsorgerplikt innhentSystemverdiForsorgerplikt(String personIdentifikator) {
        JsonForsorgerplikt jsonForsorgerplikt = new JsonForsorgerplikt();
        if (jsonForsorgerplikt.getHarForsorgerplikt() == null){
            jsonForsorgerplikt.setHarForsorgerplikt(new JsonHarForsorgerplikt());
        }

        List<Barn> barn = personService.hentBarn(personIdentifikator);
        if (barn == null || barn.isEmpty()) {
            jsonForsorgerplikt.getHarForsorgerplikt()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(false);
            return jsonForsorgerplikt;
        }
        jsonForsorgerplikt.getHarForsorgerplikt()
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(true);
        jsonForsorgerplikt.setAnsvar(barn.stream().map(this::mapToJsonAnsvar)
                .collect(Collectors.toList()));
        return jsonForsorgerplikt;
    }

    private JsonAnsvar mapToJsonAnsvar(Barn barn) {
        JsonBarn jsonBarn = new JsonBarn();
        if(barn.harIkkeTilgang() != null && barn.harIkkeTilgang()){
            jsonBarn.withKilde(JsonKilde.SYSTEM)
                    .withNavn(new JsonNavn()
                            .withFornavn("")
                            .withMellomnavn("")
                            .withEtternavn(""))
                    .withHarDiskresjonskode(true);
            return new JsonAnsvar()
                    .withBarn(jsonBarn);
        }
        jsonBarn.withKilde(JsonKilde.SYSTEM)
                .withNavn(new JsonNavn()
                        .withFornavn(barn.getFornavn())
                        .withMellomnavn(barn.getMellomnavn())
                        .withEtternavn(barn.getEtternavn()))
                .withFodselsdato(barn.getFodselsdato() != null ? barn.getFodselsdato().toString() : null)
                .withPersonIdentifikator(barn.getFnr())
                .withHarDiskresjonskode(false);
        return new JsonAnsvar()
                .withBarn(jsonBarn).withErFolkeregistrertSammen(new JsonErFolkeregistrertSammen()
                        .withKilde(JsonKildeSystem.SYSTEM)
                        .withVerdi(barn.erFolkeregistrertsammen()));
    }

}
