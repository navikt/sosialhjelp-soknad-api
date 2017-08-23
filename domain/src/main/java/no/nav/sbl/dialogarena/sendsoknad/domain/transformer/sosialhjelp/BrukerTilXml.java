package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLPersonalia;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLPersonalia.Person;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Map;
import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.SYSTEM;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilXMLString;


public class BrukerTilXml implements Function<WebSoknad, XMLPersonalia> {

    @Override
    public XMLPersonalia apply(WebSoknad webSoknad) {
        String personnummer = webSoknad.getAktoerId();
        Map<String, String> personaliaProperties = webSoknad.getFaktumMedKey("personalia").getProperties();

        return new XMLPersonalia().withPerson(new Person()
                .withPersonIdentifikator(tilXMLString(personnummer, SYSTEM))
                .withNavn(tilXMLString(personaliaProperties.get("navn"), SYSTEM))
                .withStatsborgerskap(tilXMLString(personaliaProperties.get("statsborgerskap"), SYSTEM))
                .withEpostAdresse(tilXMLString(personaliaProperties.get("epost") == null ? "ingen epost" : personaliaProperties.get("epost"), SYSTEM))
                .withAdresse(tilXMLString(personaliaProperties.get("gjeldendeAdresse"), SYSTEM))
        );
    }
}