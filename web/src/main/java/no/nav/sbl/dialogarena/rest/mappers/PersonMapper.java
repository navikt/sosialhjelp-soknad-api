package no.nav.sbl.dialogarena.rest.mappers;

import no.nav.sbl.dialogarena.rest.ressurser.NavnFrontend;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;

public class PersonMapper {
    public static String getPersonnummerFromFnr(String fnr){
        return fnr != null ? fnr.substring(6) : null;
    }

    public static JsonNavn mapToJsonNavn(NavnFrontend navn) {
        if (navn == null){
            return null;
        }
        return new JsonNavn()
                .withFornavn(navn.fornavn != null ? navn.fornavn : "")
                .withMellomnavn(navn.mellomnavn != null ? navn.mellomnavn : "")
                .withEtternavn(navn.etternavn != null ? navn.etternavn : "");
    }
}
