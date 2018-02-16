package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import com.google.common.collect.ImmutableMap;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class KommuneTilNavEnhetMapper {

    public static class NavEnhet {

        private String navn;
        private String orgnummer;

        NavEnhet(String navn, String orgnummer) {
            this.navn = navn;
            this.orgnummer = orgnummer;
        }

        public String getOrgnummer() {
            return orgnummer;
        }

        public String getNavn() {
            return navn;
        }
    }


    private static final Map<String, NavEnhet> map = new ImmutableMap.Builder<String, NavEnhet>()

            // GAMMEL
            /*
            .put("horten", new NavEnhet("NAV Horten", "974605171"))  //PROD
            .put("frogner", new NavEnhet("NAV Oslo Bydel Frogner", "910229699")) // TEST
            .put("grunerlokka", new NavEnhet("NAV Oslo Bydel Grünerløkka", "974778866")) // PROD
            .put("grorud", new NavEnhet("NAV Oslo Bydel Grünerløkka", "910229702")) // TEST
            */

            // PROD

            .put("horten", new NavEnhet("NAV Horten", "974605171"))
            .put("bergenhus", new NavEnhet("NAV Bergen Bydel Bergenhus", "976830563")) //OK
            .put("ytrebygda", new NavEnhet("NAV Bergen Bydel Ytrebygda", "976830652")) //OK
            //.put("frogner", new NavEnhet("NAV Oslo Bydel Frogner", "874778702")) //OK
            //.put("grunerlokka", new NavEnhet("NAV Oslo Bydel Grünerløkka", "870534612")) //OK
            //.put("grorud", new NavEnhet("NAV Oslo Bydel Grorud", "974778866")) //OK



            // TEST
            /*
            .put("horten", new NavEnhet("NAV Horten", "910940066"))
            .put("bergenhus", new NavEnhet("NAV Bergen Bydel Bergenhus", "910230158")) //OK
            .put("ytrebygda", new NavEnhet("NAV Bergen Bydel Ytrebygda", "910230158")) //OK
            .put("frogner", new NavEnhet("NAV Oslo Bydel Frogner", "910229699")) // OK
            //.put("grunerlokka", new NavEnhet("NAV Oslo Bydel Grünerløkka", "974778866")) // TAS UT FORELØPIG SIDEN DE IKKE HAR TESTMILJØ
            .put("grorud", new NavEnhet("NAV Oslo Bydel Grorud", "910229702")) // OK
            */
            .build();


    public static NavEnhet getNavEnhetFromWebSoknad(WebSoknad webSoknad) {
        String key;
        if (webSoknad.getFaktumMedKey("personalia.bydel") == null || isEmpty(webSoknad.getFaktumMedKey("personalia.bydel").getValue())) {
            key = webSoknad.getFaktumMedKey("personalia.kommune").getValue();
        } else {
            key = webSoknad.getFaktumMedKey("personalia.bydel").getValue();
        }

        NavEnhet navEnhet = map.get(key);
        if (navEnhet == null) {
            throw new IllegalStateException("Innsendt kommune/bydel har ikke NAVEnhet");
        }
        return navEnhet;
    }

}
