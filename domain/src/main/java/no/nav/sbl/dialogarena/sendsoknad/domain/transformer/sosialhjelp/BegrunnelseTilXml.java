package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBegrunnelse;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.*;

public class BegrunnelseTilXml implements Function<WebSoknad, XMLBegrunnelse> {

    @Override
    public XMLBegrunnelse apply(WebSoknad webSoknad) {
        return new XMLBegrunnelse()
                .withHvorforSoke(tilString(webSoknad, "begrunnelse.hvorfor"))
                .withHvaSokesOm(tilString(webSoknad, "begrunnelse.hva"));
    }
}