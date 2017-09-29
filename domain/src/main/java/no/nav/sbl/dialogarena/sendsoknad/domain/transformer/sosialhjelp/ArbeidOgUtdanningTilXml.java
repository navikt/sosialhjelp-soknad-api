package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLArbeidUtdanning;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

public class ArbeidOgUtdanningTilXml implements Function<WebSoknad, XMLArbeidUtdanning> {

    @Override
    public XMLArbeidUtdanning apply(WebSoknad webSoknad) {
        XMLArbeidUtdanning arbeidUtdanning = new XMLArbeidUtdanning();

        return arbeidUtdanning;
    }
}