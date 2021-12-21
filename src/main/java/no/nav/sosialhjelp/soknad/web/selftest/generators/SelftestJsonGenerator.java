//package no.nav.sosialhjelp.soknad.web.selftest.generators;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import no.nav.sosialhjelp.soknad.web.selftest.domain.Selftest;
//
///*
//Kopiert inn fra no.nav.sbl.dialogarena:common-web
//Endringer gjort i no.nav.common:web gjør at vi heller benytter den fra det gamle artefaktet.
//Kan mest sannsynlig oppgraderes, hvis vi får selftest til å fungere fra no.nav.common:web
//*/
//
//public final class SelftestJsonGenerator {
//
//    private SelftestJsonGenerator() {
//    }
//
//    public static String generate(Selftest selftest) throws JsonProcessingException {
//        ObjectMapper om = new ObjectMapper();
//        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        return om.writeValueAsString(selftest);
//    }
//}