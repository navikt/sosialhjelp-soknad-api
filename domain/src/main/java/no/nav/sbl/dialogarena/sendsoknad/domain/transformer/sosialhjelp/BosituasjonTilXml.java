package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.*;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBosituasjon;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBosituasjon.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilXMLBoolean;


public class BosituasjonTilXml implements Function<WebSoknad, XMLBosituasjon> {

    @Override
    public XMLBosituasjon apply(WebSoknad webSoknad) {

        Botype botype = new Botype()
                .withKilde(BRUKER);

        String botypeVerdi = webSoknad.getValueForFaktum("bosituasjon");
        switch (botypeVerdi) {
            case "eier":
                botype.withValue(XMLBotype.EIER);
            case "leierpivat":
                botype.withValue(XMLBotype.LEIER);
            case "leierkommunalt":
                botype.withValue(XMLBotype.KOMMUNAL);
                case "ingen":
                botype.withValue(XMLBotype.INGEN);
            case "annet":
                botype.withValue(XMLBotype.ANNEN);
        }

        XMLBosituasjon xmlBosituasjon = new XMLBosituasjon();

        if(botypeVerdi.equals("annet")) {
            xmlBosituasjon.withInstitusjon(tilXMLBoolean(webSoknad, "bosituasjon.annen.botype.institusjon"));
            xmlBosituasjon.withFengsel(tilXMLBoolean(webSoknad, "bosituasjon.annen.botype.fengsel"));
            xmlBosituasjon.withForeldre(tilXMLBoolean(webSoknad, "bosituasjon.annen.botype.foreldre"));
            xmlBosituasjon.withKrisesenter(tilXMLBoolean(webSoknad, "bosituasjon.annen.botype.krisesenter"));
            xmlBosituasjon.withVenner(tilXMLBoolean(webSoknad, "bosituasjon.annen.botype.venner"));
            xmlBosituasjon.withFamilie(tilXMLBoolean(webSoknad, "bosituasjon.annen.botype.familie"));
        }

        return xmlBosituasjon.withBotype(botype);
    }
}