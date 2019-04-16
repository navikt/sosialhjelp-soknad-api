package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning.Studentgrad;
import org.slf4j.Logger;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.erTom;
import static org.slf4j.LoggerFactory.getLogger;

public final class JsonUtdanningConverter {

    private static final Logger logger = getLogger(JsonUtdanningConverter.class);

    private JsonUtdanningConverter() {

    }

    public static JsonUtdanning tilUtdanning(WebSoknad webSoknad) {
        JsonUtdanning jsonUtdanning = new JsonUtdanning();
        jsonUtdanning.setKilde(JsonKilde.BRUKER);

        String studerer = webSoknad.getValueForFaktum("dinsituasjon.studerer");
        if (JsonUtils.erIkkeTom(studerer)) {
            jsonUtdanning.setErStudent(Boolean.parseBoolean(studerer));
        }

        String studentgrad = webSoknad.getValueForFaktum("dinsituasjon.studerer.true.grad");
        jsonUtdanning.setStudentgrad(tilStudentgrad(studentgrad));

        return jsonUtdanning;
    }

    private static Studentgrad tilStudentgrad(String s) {
        if (erTom(s)) {
            return null;
        }

        switch (s) {
            case "heltid":
                return Studentgrad.HELTID;
            case "deltid":
                return Studentgrad.DELTID;
        }

        logger.warn("Ukjent studentgrad: {}", s);
        return null;
    }

}
