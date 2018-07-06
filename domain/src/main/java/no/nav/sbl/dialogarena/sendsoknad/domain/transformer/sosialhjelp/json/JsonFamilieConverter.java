package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.*;
import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER;
import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.SYSTEM;
import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.GIFT;
import static org.slf4j.LoggerFactory.getLogger;

public final class JsonFamilieConverter {

    private static final Logger logger = getLogger(JsonFamilieConverter.class);

    private JsonFamilieConverter() {

    }

    public static JsonFamilie tilFamilie(WebSoknad webSoknad) {
        final JsonFamilie jsonFamilie = new JsonFamilie();
        jsonFamilie.setSivilstatus(tilJsonSivilstatus(webSoknad));
        jsonFamilie.setForsorgerplikt(tilJsonForsorgerPlikt(webSoknad));

        return jsonFamilie;
    }

    static JsonSivilstatus tilJsonSivilstatus(WebSoknad webSoknad) {
        final String brukerregistrertSivilstatus = webSoknad.getValueForFaktum("familie.sivilstatus");
        final String systemregistrertSivilstatus = webSoknad.getValueForFaktum("system.familie.sivilstatus");
        if (erTom(systemregistrertSivilstatus) & erTom(brukerregistrertSivilstatus)) {
            return null;
        } else if (!erTom(systemregistrertSivilstatus) & erTom(brukerregistrertSivilstatus)) {
            return tilJsonSystemregistrertSivilstatus(webSoknad);
        } else {
            return tilJsonBrukerregistrertSivilstatus(webSoknad);
        }
    }

    private static JsonSivilstatus tilJsonSystemregistrertSivilstatus(WebSoknad webSoknad) {
        String sivilstatus = webSoknad.getValueForFaktum("system.familie.sivilstatus");
        final JsonSivilstatus jsonSivilstatus = new JsonSivilstatus();
        jsonSivilstatus.setKilde(SYSTEM);

        final Status status = tilStatus(sivilstatus);
        jsonSivilstatus.setStatus(status);

        if (status == GIFT) {
            final Map<String, String> ektefelle = getEktefelleproperties(webSoknad, "system.familie.sivilstatus.gift.ektefelle");

        }

        return jsonSivilstatus;
    }

    private static JsonSivilstatus tilJsonBrukerregistrertSivilstatus(WebSoknad webSoknad) {
        final String sivilstatus = webSoknad.getValueForFaktum("familie.sivilstatus");

        final JsonSivilstatus jsonSivilstatus = new JsonSivilstatus();
        jsonSivilstatus.setKilde(BRUKER);

        final Status status = tilStatus(sivilstatus);
        jsonSivilstatus.setStatus(status);

        if (status == GIFT) {
            final Map<String, String> ektefelle = getEktefelleproperties(webSoknad, "familie.sivilstatus.gift.ektefelle");
            jsonSivilstatus.setEktefelle(tilJsonEktefelle(ektefelle));

            final String borSammenMed = ektefelle.get("borsammen");
            if (JsonUtils.erIkkeTom(borSammenMed)) {
                jsonSivilstatus.setBorSammenMed(Boolean.valueOf(borSammenMed));
            }

            final String borIkkeSammenMedBegrunnelse = ektefelle.get("ikkesammenbeskrivelse");
            if (JsonUtils.erIkkeTom(borIkkeSammenMedBegrunnelse)) {
                jsonSivilstatus.setBorIkkeSammenMedBegrunnelse(borIkkeSammenMedBegrunnelse);
            }
        }

        return jsonSivilstatus;
    }

    private static Map<String, String> getEktefelleproperties(WebSoknad webSoknad, String faktumKey) {
        final Faktum faktum = webSoknad.getFaktumMedKey(faktumKey);
        if (faktum == null) {
            return new HashMap<>();
        }
        return faktum.getProperties();
    }

    private static JsonEktefelle tilJsonEktefelle(Map<String, String> ektefelle) {
        final JsonEktefelle jsonEktefelle = new JsonEktefelle();
        jsonEktefelle.setNavn(new JsonNavn()
                .withFornavn(xxxFornavnFraNavn(ektefelle.get("navn")))
                .withMellomnavn("")
                .withEtternavn(xxxEtternavnFraNavn(ektefelle.get("navn")))
        );
        jsonEktefelle.setFodselsdato(tilJsonFodselsdato(ektefelle.get("fnr"))); // XXX: "Fødselsdato kan ikke hete "fnr" og må bytte navn.
        jsonEktefelle.setPersonIdentifikator(tilJsonPersonidentifikator(ektefelle.get("fnr"), ektefelle.get("pnr")));

        return jsonEktefelle;
    }


    private static Status tilStatus(String sivilstatus) {
        final Status status = Status.fromValue(sivilstatus);
        if (status == null) {
            logger.warn("Ukjent sivilstatus: {}", sivilstatus);
        }
        return status;
    }


    private static JsonForsorgerplikt tilJsonForsorgerPlikt(WebSoknad webSoknad) {
        return new JsonForsorgerplikt()
                .withHarForsorgerplikt(tilHarForsorgerPlikt(webSoknad))
                .withBarnebidrag(tilBarnebidrag(webSoknad))
                .withAnsvar(tilAnsvar(webSoknad));
    }

    private static JsonHarForsorgerplikt tilHarForsorgerPlikt(WebSoknad webSoknad) {
        String harBarn = webSoknad.getValueForFaktum("familie.barn");

        if (erTom(harBarn)) {
            return null;
        }

        return new JsonHarForsorgerplikt()
                .withKilde(BRUKER)
                .withVerdi(Boolean.parseBoolean(harBarn));
    }

    private static JsonBarnebidrag tilBarnebidrag(WebSoknad webSoknad) {
        String barnebidrag = webSoknad.getValueForFaktum("familie.barn.true.barnebidrag");

        if (erTom(barnebidrag)) {
            return null;
        }

        return new JsonBarnebidrag()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(tilBarnebidragVerdi(barnebidrag));
    }

    private static JsonBarnebidrag.Verdi tilBarnebidragVerdi(String s) {
        switch (s) {
            case "betaler":
                return JsonBarnebidrag.Verdi.BETALER;
            case "mottar":
                return JsonBarnebidrag.Verdi.MOTTAR;
            case "begge":
                return JsonBarnebidrag.Verdi.BEGGE;
            case "ingen":
                return JsonBarnebidrag.Verdi.INGEN;
        }
        logger.warn("Ukjent barnebidragsvalg {}", s);
        return null;
    }

    private static List<JsonAnsvar> tilAnsvar(WebSoknad webSoknad) {
        List<Faktum> barnefakta = webSoknad.getFaktaMedKey("familie.barn.true.barn");

        return barnefakta.stream().map(JsonFamilieConverter::faktumTilAnsvar).collect(Collectors.toList());
    }

    private static JsonAnsvar faktumTilAnsvar(Faktum faktum) {
        Map<String, String> props = faktum.getProperties();
        JsonBarn barn = new JsonBarn()
                .withKilde(BRUKER)
                .withNavn(new JsonNavn()
                        .withFornavn(xxxFornavnFraNavn(props.get("navn")))
                        .withMellomnavn("")
                        .withEtternavn(xxxEtternavnFraNavn(props.get("navn")))
                )
                .withFodselsdato(tilJsonFodselsdato(props.get("fnr")))
                .withPersonIdentifikator(tilJsonPersonidentifikator(props.get("fnr"), props.get("pnr")));

        JsonAnsvar ansvar = new JsonAnsvar()
                .withBarn(barn);

        if (erIkkeTom(props.get("borsammen"))) {
            boolean borsammen = Boolean.parseBoolean(props.get("borsammen"));
            ansvar.withBorSammenMed(
                    new JsonBorSammenMed()
                            .withVerdi(borsammen)
                            .withKilde(JsonKildeBruker.BRUKER)
            );

            if (!borsammen) {
                if (erIkkeTom(props.get("grad"))) {
                    ansvar.withSamvarsgrad(
                            new JsonSamvarsgrad()
                                    .withVerdi(tilInteger(props.get("grad")))
                                    .withKilde(JsonKildeBruker.BRUKER)
                    );
                }
            }
        }

        return ansvar;
    }


    private static String tilJsonPersonidentifikator(String fodselsdato, String personnummer) {
        if (erTom(fodselsdato) || fodselsdato.length() != 8 || erTom(personnummer)) {
            return null;
        }

        return fodselsdato.substring(0, 4) + fodselsdato.substring(6) + personnummer;
    }

    private static String tilJsonFodselsdato(String fodselsdato) {
        if (erTom(fodselsdato)) {
            return null;
        }

        if (fodselsdato.length() != 8) {
            logger.warn("Feil lengde på fodselsdato, {}", fodselsdato);
            return null;
        }

        return fodselsdato.substring(4) + "-" + fodselsdato.substring(2, 4) + "-" + fodselsdato.substring(0, 2);
    }

    private static String xxxFornavnFraNavn(String navn) {
        // TODO: Fjern når navn er oppdelt slik det skal i grensesnittet.
        if (navn == null) {
            return "";
        }

        final String trimmedNavn = navn.trim();
        if (!trimmedNavn.contains(" ")) {
            return navn;
        }
        return trimmedNavn.substring(0, trimmedNavn.lastIndexOf(' '));
    }

    private static String xxxEtternavnFraNavn(String navn) {
        // TODO: Fjern når navn er oppdelt slik det skal i grensesnittet.
        if (navn == null || !navn.trim().contains(" ")) {
            return "";
        }

        final String trimmedNavn = navn.trim();
        return trimmedNavn.substring(trimmedNavn.lastIndexOf(' ') + 1);
    }
}
