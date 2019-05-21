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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.GIFT;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class FamilieSystemdata implements Systemdata {

    @Inject
    private PersonaliaFletter personaliaFletter;

    @Inject
    private PersonService personService;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        final JsonData jsonData = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData();
        final String personIdentifikator = jsonData.getPersonalia().getPersonIdentifikator().getVerdi();
        final JsonFamilie familie = jsonData.getFamilie();
        if (familie.getSivilstatus() == null || familie.getSivilstatus().getKilde() == JsonKilde.SYSTEM) {
            final JsonSivilstatus systemverdiSivilstatus = innhentSystemverdiSivilstatus(personIdentifikator);
            if (systemverdiSivilstatus != null) {
                familie.setSivilstatus(systemverdiSivilstatus);
            }
        }

        JsonForsorgerplikt forsorgerplikt = familie.getForsorgerplikt();
        JsonHarForsorgerplikt harForsorgerplikt = forsorgerplikt.getHarForsorgerplikt();
        if (harForsorgerplikt == null || harForsorgerplikt.getKilde() == null ||
                harForsorgerplikt.getKilde() == JsonKilde.SYSTEM) {
            JsonForsorgerplikt systemverdiForsorgerplikt = innhentSystemverdiForsorgerplikt(personIdentifikator);

            if (systemverdiForsorgerplikt.getHarForsorgerplikt().getVerdi()) {
                forsorgerplikt.setHarForsorgerplikt(systemverdiForsorgerplikt.getHarForsorgerplikt());

                List<JsonAnsvar> ansvarList = forsorgerplikt.getAnsvar();
                if (ansvarList != null && !ansvarList.isEmpty()) {
                    ansvarList.removeIf(jsonAnsvar -> isNotInList(jsonAnsvar, systemverdiForsorgerplikt.getAnsvar()));
                    ansvarList.addAll(systemverdiForsorgerplikt.getAnsvar().stream()
                            .filter(sysAnsvar -> isNotInList(sysAnsvar, forsorgerplikt.getAnsvar()))
                            .collect(Collectors.toList()));
                } else {
                    forsorgerplikt.setAnsvar(systemverdiForsorgerplikt.getAnsvar());
                }

            } else {
                forsorgerplikt.setHarForsorgerplikt(systemverdiForsorgerplikt.getHarForsorgerplikt());
                forsorgerplikt.setBarnebidrag(null);
                forsorgerplikt.setAnsvar(new ArrayList<>());
            }
        }
    }

    private boolean isNotInList(JsonAnsvar jsonAnsvar, List<JsonAnsvar> jsonAnsvarList) {
        return jsonAnsvarList.stream().noneMatch(
                ansvar -> {
                    if (ansvar.getBarn() == null) {
                        throw new IllegalStateException("JsonAnsvar mangler barn. Ikke mulig å skille fra andre barn");
                    }
                    if (ansvar.getBarn().getPersonIdentifikator() != null) {
                        return ansvar.getBarn().getPersonIdentifikator().equals(jsonAnsvar.getBarn().getPersonIdentifikator());
                    } else {
                        return ansvar.getBarn().getNavn().equals(jsonAnsvar.getBarn().getNavn());
                    }
                }
        );
    }

    private JsonSivilstatus innhentSystemverdiSivilstatus(String personIdentifikator) {
        final Personalia personalia = personaliaFletter.mapTilPersonalia(personIdentifikator);
        if (personalia == null || isEmpty(personalia.getSivilstatus()) || personalia.getEktefelle() == null) {
            return null;
        }
        JsonSivilstatus jsonSivilstatus = new JsonSivilstatus()
                .withKilde(JsonKilde.SYSTEM)
                .withStatus(JsonSivilstatus.Status.fromValue(personalia.getSivilstatus()));

        Ektefelle ektefelle = personalia.getEktefelle();
        if (jsonSivilstatus.getStatus().equals(GIFT)) {
            jsonSivilstatus
                    .withEktefelle(tilSystemregistrertJsonEktefelle(ektefelle))
                    .withEktefelleHarDiskresjonskode(ektefelle == null ? null : ektefelle.harIkketilgangtilektefelle())
                    .withFolkeregistrertMedEktefelle(ektefelle == null ? null : ektefelle.erFolkeregistrertsammen());
        }
        return jsonSivilstatus;
    }

    private static JsonEktefelle tilSystemregistrertJsonEktefelle(Ektefelle ektefelle) {
        if (ektefelle == null || ektefelle.harIkketilgangtilektefelle()) {
            return new JsonEktefelle().withNavn(new JsonNavn()
                    .withFornavn("")
                    .withMellomnavn("")
                    .withEtternavn(""));
        }
        return new JsonEktefelle()
                .withNavn(mapToJsonNavn(ektefelle))
                .withFodselsdato(ektefelle.getFodselsdato() != null ? ektefelle.getFodselsdato().toString() : null)
                .withPersonIdentifikator(ektefelle.getFnr());
    }

    private static JsonNavn mapToJsonNavn(Ektefelle ektefelle) {
        return new JsonNavn()
                .withFornavn(ektefelle.getFornavn() != null ? ektefelle.getFornavn() : "")
                .withMellomnavn(ektefelle.getMellomnavn() != null ? ektefelle.getMellomnavn() : "")
                .withEtternavn(ektefelle.getEtternavn() != null ? ektefelle.getEtternavn() : "");
    }

    private JsonForsorgerplikt innhentSystemverdiForsorgerplikt(String personIdentifikator) {
        JsonForsorgerplikt jsonForsorgerplikt = new JsonForsorgerplikt().withHarForsorgerplikt(new JsonHarForsorgerplikt());

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
        return new JsonAnsvar()
                .withBarn(new JsonBarn()
                        .withKilde(JsonKilde.SYSTEM)
                        .withNavn(new JsonNavn()
                                .withFornavn(barn.getFornavn())
                                .withMellomnavn(barn.getMellomnavn())
                                .withEtternavn(barn.getEtternavn()))
                        .withFodselsdato(barn.getFodselsdato() != null ? barn.getFodselsdato().toString() : null)
                        .withPersonIdentifikator(barn.getFnr())
                        .withHarDiskresjonskode(false))
                .withErFolkeregistrertSammen(new JsonErFolkeregistrertSammen()
                        .withKilde(JsonKildeSystem.SYSTEM)
                        .withVerdi(barn.erFolkeregistrertsammen()));
    }

}
