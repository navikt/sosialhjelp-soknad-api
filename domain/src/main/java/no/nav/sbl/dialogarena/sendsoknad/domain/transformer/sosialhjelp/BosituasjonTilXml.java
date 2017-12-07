package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBosituasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilInteger;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilString;


public class BosituasjonTilXml implements Function<WebSoknad, XMLBosituasjon> {

    @Override
    public XMLBosituasjon apply(WebSoknad webSoknad) {
        return new XMLBosituasjon()
                .withBoType(tilString(webSoknad, "bosituasjon"))
                .withAnnenSituasjon(tilString(webSoknad, "bosituasjon.annet.botype"))
                .withAntallPersoner(tilInteger(webSoknad, "bosituasjon.antallpersoner"));
    }
}