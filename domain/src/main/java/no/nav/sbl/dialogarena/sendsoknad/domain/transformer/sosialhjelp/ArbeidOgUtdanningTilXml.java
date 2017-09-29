package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLArbeidUtdanning;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SoknadSosialhjelpUtils.tilString;

public class ArbeidOgUtdanningTilXml implements Function<WebSoknad, XMLArbeidUtdanning> {

    @Override
    public XMLArbeidUtdanning apply(WebSoknad webSoknad) {
        return new XMLArbeidUtdanning()
                .withErIJobb(tilString(webSoknad, "dinsituasjon.jobb"))
                .withJobbGrad(tilString(webSoknad, "dinsituasjon.jobb.true.grad"))
                .withErStudent(tilString(webSoknad, "dinsituasjon.studerer"))
                .withStudentGrad(tilString(webSoknad, "dinsituasjon.studerer.true.grad"));
    }
}