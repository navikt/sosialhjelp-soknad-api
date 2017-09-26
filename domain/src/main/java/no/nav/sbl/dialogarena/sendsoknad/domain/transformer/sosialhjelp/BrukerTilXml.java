package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLPersonalia;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Map;
import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.SYSTEM;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.*;


public class BrukerTilXml implements Function<WebSoknad, XMLPersonalia> {

    @Override
    public XMLPersonalia apply(WebSoknad webSoknad) {
        String personnummer = webSoknad.getAktoerId();
        Map<String, String> personaliaProperties = webSoknad.getFaktumMedKey("personalia").getProperties();

        return new XMLPersonalia()
                .withPersonIdentifikator(tilString(personnummer, SYSTEM))
//                .withNavn(tilXMLKildeString(personaliaProperties.get("navn"), SYSTEM))
                .withStatsborgerskap(tilString(personaliaProperties.get("statsborgerskap"), SYSTEM));
//                .withEpostAdresse(tilXMLKildeString(personaliaProperties.get("epost") == null ? "ingen epost" : personaliaProperties.get("epost"), SYSTEM))
//                .withAdresse(tilXMLKildeString(personaliaProperties.get("gjeldendeAdresse"), SYSTEM))
    }
}