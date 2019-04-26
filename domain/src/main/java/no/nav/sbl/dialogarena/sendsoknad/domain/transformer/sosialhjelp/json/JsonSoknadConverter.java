package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.InputSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;

import java.util.Collections;

public final class JsonSoknadConverter {

    private JsonSoknadConverter() {

    }

    public static JsonSoknad tilJsonSoknad(InputSource inputSource) {
        final JsonSoknad jsonSoknad = new JsonSoknad();
        jsonSoknad.setData(tilData(inputSource));
        jsonSoknad.setKompatibilitet(Collections.emptyList());

        jsonSoknad.setDriftsinformasjon(settDriftsinformasjonForUtbetalingSystemfeil(inputSource.getWebSoknad()));

        return jsonSoknad;
    }

    private static JsonData tilData(InputSource inputSource) {

        WebSoknad webSoknad = inputSource.getWebSoknad();

        return new JsonData()
                .withPersonalia(JsonPersonaliaConverter.tilPersonalia(webSoknad))
                .withArbeid(JsonArbeidConverter.tilArbeid(webSoknad))
                .withUtdanning(JsonUtdanningConverter.tilUtdanning(webSoknad))
                .withFamilie(JsonFamilieConverter.tilFamilie(webSoknad))
                .withBegrunnelse(JsonBegrunnelseConverter.tilBegrunnelse(webSoknad))
                .withBosituasjon(JsonBosituasjonConverter.tilBosituasjon(webSoknad))
                .withOkonomi(JsonOkonomiConverter.tilOkonomi(inputSource));
    }

    private static String settDriftsinformasjonForUtbetalingSystemfeil(WebSoknad webSoknad) {
        final Faktum faktumMedKeyNav = webSoknad.getFaktumMedKey("utbetalinger.fra.nav.feilet");
        if (faktumMedKeyNav != null && "true".equalsIgnoreCase(faktumMedKeyNav.getValue())) {
            return "Kunne ikke hente utbetalinger fra NAV";
        }

        final Faktum faktumMedKeySkatt = webSoknad.getFaktumMedKey("utbetalinger.fra.skatteetaten.feilet");
        if (faktumMedKeySkatt != null && "true".equalsIgnoreCase(faktumMedKeySkatt.getValue())) {
            return "Kunne ikke hente utbetalinger fra Skatteetaten";
        }

        return "";
    }
}
