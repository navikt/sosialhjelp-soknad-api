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


    private static final Map<String, NavEnhet> map = ImmutableMap.of(
            "horten", new NavEnhet("NAV Horten", "123456798"),
            "frogner", new NavEnhet("NAV Frogner", "123456789")
    );

    public static NavEnhet getNavEnhetFromWebSoknad(WebSoknad webSoknad) {
        String key;
        if (webSoknad.getFaktumMedKey("personalia.bydel") == null || isEmpty(webSoknad.getFaktumMedKey("personalia.bydel").getValue())) {
            key = webSoknad.getFaktumMedKey("personalia.kommune").getValue();
        } else {
            key = webSoknad.getFaktumMedKey("personalia.bydel").getValue();
        }

        NavEnhet navEnhet = map.get(key);
        if( navEnhet == null) {
            throw new IllegalStateException("Innsendt kommune/bydel har ikke NAVEnhet");
        }
        return navEnhet;
    }

}
