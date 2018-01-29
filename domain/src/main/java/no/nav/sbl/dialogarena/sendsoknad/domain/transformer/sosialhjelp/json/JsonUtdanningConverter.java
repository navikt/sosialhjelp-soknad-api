package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning.Studentgrad;

public final class JsonUtdanningConverter {
    
    private static final Logger logger = getLogger(JsonUtdanningConverter.class);
    
    
    private JsonUtdanningConverter() {
        
    }
    

    public static JsonUtdanning toUtdanning(WebSoknad webSoknad) {
        final JsonUtdanning jsonUtdanning = new JsonUtdanning();
        jsonUtdanning.setKilde(JsonKilde.BRUKER);
        
        final String studerer = webSoknad.getValueForFaktum("dinsituasjon.studerer");
        if (JsonUtils.nonEmpty(studerer)) {
            jsonUtdanning.setErStudent(Boolean.parseBoolean(studerer));
        }
        
        final String studentgrad = webSoknad.getValueForFaktum("dinsituasjon.studerer.true.grad");
        jsonUtdanning.setStudentgrad(toStudentgrad(studentgrad));
        
        return jsonUtdanning;
    }
    
    private static Studentgrad toStudentgrad(String s) {
        if (s == null || s.trim().equals("")) {
            return null;
        }
        
        switch (s) {
        case "heltid": return Studentgrad.HELTID;
        case "deltid": return Studentgrad.DELTID;
        }
        
        logger.error("Ukjent studentgrad: " + s);
        return null;
    }

}
