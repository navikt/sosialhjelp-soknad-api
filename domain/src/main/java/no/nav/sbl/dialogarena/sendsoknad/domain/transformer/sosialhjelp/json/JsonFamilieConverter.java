package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public final class JsonFamilieConverter {

    private static final Logger logger = getLogger(JsonFamilieConverter.class);

    private JsonFamilieConverter() {

    }

    public static JsonFamilie toFamilie(WebSoknad webSoknad) {
        final JsonFamilie jsonFamilie = new JsonFamilie();
        jsonFamilie.setSivilstatus(toJsonSivilstatus(webSoknad));
        // TODO: Fortsette på familie...

        return jsonFamilie;
    }

    private static JsonSivilstatus toJsonSivilstatus(WebSoknad webSoknad) {
        final String sivilstatus = webSoknad.getValueForFaktum("familie.sivilstatus");
        if (JsonUtils.empty(sivilstatus)) {
            return null;
        }

        final JsonSivilstatus jsonSivilstatus = new JsonSivilstatus();
        jsonSivilstatus.setKilde(JsonKilde.BRUKER);

        final Status status = toStatus(sivilstatus);
        jsonSivilstatus.setStatus(status);

        if (status == Status.GIFT) {
            final Map<String, String> ektefelle = getEktefelleproperties(webSoknad);
            jsonSivilstatus.setEktefelle(toJsonEktefelle(ektefelle));

            final String borSammenMed = ektefelle.get("borsammen");
            if (JsonUtils.nonEmpty(borSammenMed)) {
                jsonSivilstatus.setBorSammenMed(Boolean.valueOf(borSammenMed));
            }

            final String borIkkeSammenMedBegrunnelse = ektefelle.get("ikkesammenbeskrivelse");
            if (JsonUtils.nonEmpty(borIkkeSammenMedBegrunnelse)) {
                jsonSivilstatus.setBorIkkeSammenMedBegrunnelse(borIkkeSammenMedBegrunnelse);
            }
        }

        return jsonSivilstatus;
    }

    private static Map<String, String> getEktefelleproperties(WebSoknad webSoknad) {
        final Faktum faktum = webSoknad.getFaktumMedKey("familie.sivilstatus.gift.ektefelle");
        if (faktum == null) {
            return new HashMap<>();
        }
        return faktum.getProperties();
    }

    private static JsonEktefelle toJsonEktefelle(Map<String, String> ektefelle) {
        final JsonEktefelle jsonEktefelle = new JsonEktefelle();
        jsonEktefelle.setNavn(new JsonNavn()
                .withFornavn(xxxFornavnFraNavn(ektefelle.get("navn")))
                .withMellomnavn("")
                .withEtternavn(xxxEtternavnFraNavn(ektefelle.get("navn")))
        );
        jsonEktefelle.setFodselsdato(toJsonFodselsdato(ektefelle.get("fnr"))); // XXX: "Fødselsdato kan ikke hete "fnr" og må bytte navn.
        jsonEktefelle.setPersonIdentifikator(toJsonPersonidentifikator(ektefelle));

        return jsonEktefelle;
    }

    private static String toJsonPersonidentifikator(Map<String, String> ektefelle) {
        // XXX: Ha et eget sammensatt felt for fødselsnummer fremfor denne galskapen...

        final String fodselsdato = ektefelle.get("fnr");
        final String personnummer = ektefelle.get("pnr");
        if (JsonUtils.empty(fodselsdato) || fodselsdato.length() != 8 || JsonUtils.empty(personnummer)) {
            return null;
        }

        return fodselsdato.substring(0, 4) + fodselsdato.substring(6) + personnummer;
    }

    private static String toJsonFodselsdato(String fodselsdato) {
        if (fodselsdato == null || fodselsdato.trim().equals("")) {
            return null;
        }

        if (fodselsdato.length() != 8) {
            logger.error("Feil lengde på fodselsdato");
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

    private static Status toStatus(String sivilstatus) {
        final Status status = Status.fromValue(sivilstatus);
        if (status == null) {
            logger.error("Ukjent sivilstatus: " + sivilstatus);
        }
        return status;
    }

}
