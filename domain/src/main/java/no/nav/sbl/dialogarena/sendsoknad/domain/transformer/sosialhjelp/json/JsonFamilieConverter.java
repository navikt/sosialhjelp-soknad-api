package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Boolean.valueOf;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.*;
import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER;
import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.SYSTEM;
import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.GIFT;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;

public final class JsonFamilieConverter {

    private static final Logger logger = getLogger(JsonFamilieConverter.class);

    private JsonFamilieConverter() {

    }

    public static JsonFamilie tilFamilie(WebSoknad webSoknad) {
        JsonFamilie jsonFamilie = new JsonFamilie();
        jsonFamilie.setSivilstatus(tilJsonSivilstatus(webSoknad));
        jsonFamilie.setForsorgerplikt(tilJsonForsorgerPlikt(webSoknad));

        return jsonFamilie;
    }

    static JsonSivilstatus tilJsonSivilstatus(WebSoknad webSoknad) {
        String brukerregistrertSivilstatus = webSoknad.getValueForFaktum("familie.sivilstatus");
        String systemregistrertSivilstatus = webSoknad.getValueForFaktum("system.familie.sivilstatus");
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
        JsonSivilstatus jsonSivilstatus = new JsonSivilstatus();
        jsonSivilstatus.setKilde(SYSTEM);

        Status status = tilStatus(sivilstatus);
        jsonSivilstatus.setStatus(status);

        if (status == GIFT) {
            Map<String, String> ektefelle = getEktefelleproperties(webSoknad, "system.familie.sivilstatus.gift.ektefelle");
            jsonSivilstatus.setEktefelle(tilSystemregistrertJsonEktefelle(ektefelle));
            jsonSivilstatus.setFolkeregistrertMedEktefelle(valueOf(ektefelle.get("folkeregistrertsammen")));
            jsonSivilstatus.setEktefelleHarDiskresjonskode(valueOf(ektefelle.get("ikketilgangtilektefelle")));
        }

        return jsonSivilstatus;
    }

    private static JsonSivilstatus tilJsonBrukerregistrertSivilstatus(WebSoknad webSoknad) {
        String sivilstatus = webSoknad.getValueForFaktum("familie.sivilstatus");

        JsonSivilstatus jsonSivilstatus = new JsonSivilstatus();
        jsonSivilstatus.setKilde(BRUKER);

        Status status = tilStatus(sivilstatus);
        jsonSivilstatus.setStatus(status);

        if (status == GIFT) {
            Map<String, String> ektefelle = getEktefelleproperties(webSoknad, "familie.sivilstatus.gift.ektefelle");
            jsonSivilstatus.setEktefelle(tilBrukerregistrertJsonEktefelle(ektefelle));

            String borSammenMed = ektefelle.get("borsammen");
            if (JsonUtils.erIkkeTom(borSammenMed)) {
                jsonSivilstatus.setBorSammenMed(valueOf(borSammenMed));
            }

            String borIkkeSammenMedBegrunnelse = ektefelle.get("ikkesammenbeskrivelse");
            if (JsonUtils.erIkkeTom(borIkkeSammenMedBegrunnelse)) {
                jsonSivilstatus.setBorIkkeSammenMedBegrunnelse(borIkkeSammenMedBegrunnelse);
            }
        }

        return jsonSivilstatus;
    }

    private static Map<String, String> getEktefelleproperties(WebSoknad webSoknad, String faktumKey) {
        Faktum faktum = webSoknad.getFaktumMedKey(faktumKey);
        if (faktum == null) {
            return new HashMap<>();
        }
        return faktum.getProperties();
    }

    private static JsonEktefelle tilSystemregistrertJsonEktefelle(Map<String, String> ektefelle) {
        String ikketilgangtilektefelle = ektefelle.get("ikketilgangtilektefelle");
        if (erIkkeTom(ikketilgangtilektefelle) && valueOf(ikketilgangtilektefelle)) {
            return new JsonEktefelle().withNavn(new JsonNavn()
                    .withFornavn("")
                    .withMellomnavn("")
                    .withEtternavn(""));
        }
        JsonEktefelle jsonEktefelle = new JsonEktefelle();
        jsonEktefelle.setNavn(mapNavnFraFaktumTilJsonNavn(ektefelle));
        jsonEktefelle.setFodselsdato(tilJsonFodselsdato(ektefelle.get("fodselsdato")));
        jsonEktefelle.setPersonIdentifikator(ektefelle.get("fnr"));

        return jsonEktefelle;
    }

    private static JsonEktefelle tilBrukerregistrertJsonEktefelle(Map<String, String> ektefelle) {
        JsonEktefelle jsonEktefelle = new JsonEktefelle();
        if (isEmpty(ektefelle.get("navn"))) {
            jsonEktefelle.setNavn(mapNavnFraFaktumTilJsonNavn(ektefelle));
        } else {
            jsonEktefelle.setNavn(new JsonNavn()
                    .withFornavn(xxxFornavnFraNavn(ektefelle.get("navn")))
                    .withMellomnavn("")
                    .withEtternavn(xxxEtternavnFraNavn(ektefelle.get("navn")))
            );
        }
        jsonEktefelle.setFodselsdato(tilJsonFodselsdato(ektefelle.get("fnr"))); // XXX: "Fødselsdato kan ikke hete "fnr" og må bytte navn.
        jsonEktefelle.setPersonIdentifikator(tilJsonPersonidentifikator(ektefelle.get("fnr"), ektefelle.get("pnr")));

        return jsonEktefelle;
    }

    private static JsonNavn mapNavnFraFaktumTilJsonNavn(Map<String, String> faktumMedNavn) {
        return new JsonNavn()
                .withFornavn(faktumMedNavn.get("fornavn") != null ? faktumMedNavn.get("fornavn") : "")
                .withMellomnavn(faktumMedNavn.get("mellomnavn") != null ? faktumMedNavn.get("mellomnavn") : "")
                .withEtternavn(faktumMedNavn.get("etternavn") != null ? faktumMedNavn.get("etternavn") : "");
    }


    private static Status tilStatus(String sivilstatus) {
        Status status = Status.fromValue(sivilstatus);
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
        String harBarn = webSoknad.getValueForFaktum("system.familie.barn");

        if (erTom(harBarn)) {
            return null;
        }

        return new JsonHarForsorgerplikt()
                .withKilde(SYSTEM)
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
        List<Faktum> barnefakta = webSoknad.getFaktaMedKey("system.familie.barn.true.barn");

        return barnefakta.stream().map(JsonFamilieConverter::faktumTilAnsvar).collect(Collectors.toList());
    }

    static JsonAnsvar faktumTilAnsvar(Faktum faktum) {
        Map<String, String> barn = faktum.getProperties();
        JsonBarn jsonBarn = new JsonBarn();
        String ikketilgangtilbarn = barn.get("ikketilgangtilbarn");
        boolean barnHarDiskresjonskode = erIkkeTom(ikketilgangtilbarn) && valueOf(ikketilgangtilbarn);
        if (barnHarDiskresjonskode) {
            jsonBarn.withKilde(SYSTEM)
                    .withNavn(new JsonNavn()
                            .withFornavn("")
                            .withMellomnavn("")
                            .withEtternavn(""))
                    .withHarDiskresjonskode(true);
        } else {
            jsonBarn.withKilde(SYSTEM)
                    .withNavn(mapNavnFraFaktumTilJsonNavn(barn))
                    .withFodselsdato(tilJsonFodselsdato(barn.get("fodselsdato")))
                    .withPersonIdentifikator(barn.get("fnr"))
                    .withHarDiskresjonskode(false);
        }
        JsonAnsvar ansvar = new JsonAnsvar()
                .withBarn(jsonBarn);

        if (!barnHarDiskresjonskode && erIkkeTom(barn.get("folkeregistrertsammen"))) {
            boolean borsammen = valueOf(barn.get("folkeregistrertsammen"));
            ansvar.withErFolkeregistrertSammen(
                    new JsonErFolkeregistrertSammen()
                            .withVerdi(borsammen)
                            .withKilde(JsonKildeSystem.SYSTEM));

            if (!borsammen) {
                if (erIkkeTom(barn.get("grad"))) {
                    ansvar.withSamvarsgrad(
                            new JsonSamvarsgrad()
                                    .withVerdi(tilInteger(barn.get("grad")))
                                    .withKilde(JsonKildeBruker.BRUKER)
                    );
                }
            } else if (erIkkeTom(barn.get("deltbosted"))) {
                boolean deltBosted = valueOf(barn.get("deltbosted"));
                ansvar.withHarDeltBosted(
                        new JsonHarDeltBosted()
                                .withVerdi(deltBosted)
                                .withKilde(JsonKildeBruker.BRUKER)
                );
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

    static String tilJsonFodselsdato(String fodselsdato) {
        if (erTom(fodselsdato)) {
            return null;
        }

        if (fodselsdato.matches("\\d{4}[-]\\d{2}[-]\\d{2}")) {
            return fodselsdato;
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

        String trimmedNavn = navn.trim();
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

        String trimmedNavn = navn.trim();
        return trimmedNavn.substring(trimmedNavn.lastIndexOf(' ') + 1);
    }
}
