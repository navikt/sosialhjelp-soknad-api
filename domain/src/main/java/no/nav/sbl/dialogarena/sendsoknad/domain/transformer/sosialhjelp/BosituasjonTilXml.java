package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBosituasjon;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.kodeverk.XMLBotype;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.*;


public class BosituasjonTilXml implements Function<WebSoknad, XMLBosituasjon> {

    @Override
    public XMLBosituasjon apply(WebSoknad webSoknad) {

        XMLBosituasjon xmlBosituasjon = new XMLBosituasjon();

        String botypeVerdi = webSoknad.getValueForFaktum("bosituasjon");
        switch (botypeVerdi) {
            case "eier":
                xmlBosituasjon.withBoType(tilString(XMLBotype.EIER.value()));
            case "leierpivat":
                xmlBosituasjon.withBoType(tilString(XMLBotype.LEIER.value()));
            case "leierkommunalt":
                xmlBosituasjon.withBoType(tilString(XMLBotype.KOMMUNAL.value()));
                case "ingen":
                xmlBosituasjon.withBoType(tilString(XMLBotype.INGEN.value()));
            case "annet":
                xmlBosituasjon.withBoType(tilString(XMLBotype.ANNET.value()));
        }


        if(botypeVerdi.equals("annet")) {
//            xmlBosituasjon.withInstitusjon(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "bosituasjon.annen.botype.institusjon"));
//            xmlBosituasjon.withFengsel(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "bosituasjon.annen.botype.fengsel"));
//            xmlBosituasjon.withForeldre(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "bosituasjon.annen.botype.foreldre"));
//            xmlBosituasjon.withKrisesenter(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "bosituasjon.annen.botype.krisesenter"));
//            xmlBosituasjon.withVenner(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "bosituasjon.annen.botype.venner"));
//            xmlBosituasjon.withFamilie(SoknadSosialhjelpUtils.tilBoolean(webSoknad, "bosituasjon.annen.botype.familie"));
        }

        return xmlBosituasjon;
    }
}