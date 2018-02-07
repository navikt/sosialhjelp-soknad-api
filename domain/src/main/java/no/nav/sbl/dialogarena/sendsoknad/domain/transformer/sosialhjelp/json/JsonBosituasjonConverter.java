package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype;
import org.slf4j.Logger;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.erIkkeTom;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.tilInteger;
import static org.slf4j.LoggerFactory.getLogger;

public final class JsonBosituasjonConverter {

    private static final Logger logger = getLogger(JsonBosituasjonConverter.class);

    private JsonBosituasjonConverter() {

    }


    public static JsonBosituasjon tilBosituasjon(WebSoknad webSoknad) {
        String situasjon = webSoknad.getValueForFaktum("bosituasjon");
        String annenSituasjon = webSoknad.getValueForFaktum("bosituasjon.annet.botype");
        String antall = webSoknad.getValueForFaktum("bosituasjon.antallpersoner");

        JsonBosituasjon bosituasjon = new JsonBosituasjon()
                .withAntallPersoner(tilInteger(antall));

        if (erIkkeTom(situasjon)) {
            bosituasjon.withBotype(erIkkeTom(annenSituasjon) ?
                    tilBotype(annenSituasjon) :
                    tilBotype(situasjon));
        }

        return bosituasjon;
    }

    private static Botype tilBotype(String verdi) {
        switch (verdi) {
            case "eier":
                return Botype.EIER;
            case "leier":
                return Botype.LEIER;
            case "kommunal":
                return Botype.KOMMUNAL;
            case "ingen":
                return Botype.INGEN;
            case "annet":
                return Botype.ANNET;
            case "institusjon":
                return Botype.INSTITUSJON;
            case "krisesenter":
                return Botype.KRISESENTER;
            case "fengsel":
                return Botype.FENGSEL;
            case "venner":
                return Botype.VENNER;
            case "foreldre":
                return Botype.FORELDRE;
            case "familie":
                return Botype.FAMILIE;
        }

        logger.warn("Ukjent botype {}", verdi);
        return null;
    }
}
