package no.nav.sosialhjelp.soknad.web.rest.mappers;

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.NavnFrontend;

public final class PersonMapper {

    private PersonMapper() {
    }

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
