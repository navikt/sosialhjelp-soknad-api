package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLPersonalia;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Map;
import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.SYSTEM;
import static no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilString;


public class BrukerTilXml implements Function<WebSoknad, XMLPersonalia> {

    @Override
    public XMLPersonalia apply(WebSoknad webSoknad) {
        String personnummer = webSoknad.getAktoerId();
        Map<String, String> personaliaProperties = webSoknad.getFaktumMedKey("personalia").getProperties();

        return new XMLPersonalia()
                .withPersonIdentifikator(tilString(personnummer, SYSTEM))
                .withStatsborgerskap(tilString(personaliaProperties.get(STATSBORGERSKAP_KEY), SYSTEM))
                .withFornavn(tilString(personaliaProperties.get(FORNAVN_KEY), SYSTEM))
                .withMellomnavn(tilString(personaliaProperties.get(MELLOMNAVN_KEY), SYSTEM))
                .withEtternavn(tilString(personaliaProperties.get(ETTERNAVN_KEY), SYSTEM))
                .withHarIkkeKontonr(tilString(webSoknad, "kontakt.kontonummer.harikke"))
                .withKontonr(tilString(webSoknad, "kontakt.kontonummer"))
                .withTelefonnr(tilString(webSoknad, "kontakt.telefon"));
    }
}