package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.person.PersonService;
import no.nav.sosialhjelp.soknad.person.domain.Barn;
import no.nav.sosialhjelp.soknad.person.domain.Ektefelle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.GIFT;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class FamilieSystemdata implements Systemdata {

    private final PersonService personService;

    public FamilieSystemdata(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        JsonData jsonData = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData();
        String personIdentifikator = jsonData.getPersonalia().getPersonIdentifikator().getVerdi();
        JsonFamilie familie = jsonData.getFamilie();
        JsonSivilstatus systemverdiSivilstatus = innhentSystemverdiSivilstatus(personIdentifikator);

        if (systemverdiSivilstatus != null || familie.getSivilstatus() == null || familie.getSivilstatus().getKilde().equals(JsonKilde.SYSTEM)) {
            familie.setSivilstatus(systemverdiSivilstatus);
        }

        JsonForsorgerplikt forsorgerplikt = familie.getForsorgerplikt();
        JsonHarForsorgerplikt harForsorgerplikt = forsorgerplikt.getHarForsorgerplikt();
        JsonForsorgerplikt systemverdiForsorgerplikt = innhentSystemverdiForsorgerplikt(personIdentifikator);

        if (systemverdiForsorgerplikt.getHarForsorgerplikt().getVerdi()) {
            forsorgerplikt.setHarForsorgerplikt(systemverdiForsorgerplikt.getHarForsorgerplikt());

            List<JsonAnsvar> ansvarList = forsorgerplikt.getAnsvar();
            if (ansvarList != null && !ansvarList.isEmpty()) {
                ansvarList.removeIf(jsonAnsvar -> jsonAnsvar.getBarn().getKilde().equals(JsonKilde.SYSTEM) &&
                        isNotInList(jsonAnsvar, systemverdiForsorgerplikt.getAnsvar()));
                ansvarList.addAll(systemverdiForsorgerplikt.getAnsvar().stream()
                        .filter(sysAnsvar -> isNotInList(sysAnsvar, forsorgerplikt.getAnsvar()))
                        .collect(Collectors.toList()));
            } else {
                forsorgerplikt.setAnsvar(systemverdiForsorgerplikt.getAnsvar());
            }

        } else if (harForsorgerplikt == null || harForsorgerplikt.getKilde().equals(JsonKilde.SYSTEM) || !harForsorgerplikt.getVerdi()) {
            forsorgerplikt.setHarForsorgerplikt(systemverdiForsorgerplikt.getHarForsorgerplikt());
            forsorgerplikt.setBarnebidrag(null);
            forsorgerplikt.setAnsvar(new ArrayList<>());
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
        var person = personService.hentPerson(personIdentifikator);
        if (person == null || isEmpty(person.getSivilstatus())) {
            return null;
        }

        Ektefelle ektefelle = person.getEktefelle();
        JsonSivilstatus.Status status = JsonSivilstatus.Status.fromValue(person.getSivilstatus());
        if (!GIFT.equals(status) || ektefelle == null) {
            return null;
        }

        return new JsonSivilstatus()
                .withKilde(JsonKilde.SYSTEM)
                .withStatus(status)
                .withEktefelle(tilSystemregistrertJsonEktefelle(ektefelle))
                .withEktefelleHarDiskresjonskode(ektefelle.getIkkeTilgangTilEktefelle())
                .withFolkeregistrertMedEktefelle(ektefelle.getFolkeregistrertSammen());
    }

    private static JsonEktefelle tilSystemregistrertJsonEktefelle(Ektefelle ektefelle) {
        if (ektefelle == null || ektefelle.getIkkeTilgangTilEktefelle()) {
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

        var barn = personService.hentBarnForPerson(personIdentifikator);
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
                        .withVerdi(barn.getFolkeregistrertSammen()));
    }

}
