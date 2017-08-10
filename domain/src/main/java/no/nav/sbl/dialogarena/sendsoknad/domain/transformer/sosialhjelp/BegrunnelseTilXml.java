package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLBegrunnelse;
import no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLString;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.function.Function;

import static no.nav.melding.domene.brukerdialog.soeknadsskjemasosialhjelp.v1.XMLKilde.BRUKER;


public class BegrunnelseTilXml implements Function<WebSoknad, XMLBegrunnelse> {

    @Override
    public XMLBegrunnelse apply(WebSoknad webSoknad) {
        XMLString hvorforSoke = new XMLString()
                .withValue(webSoknad.getValueForFaktum("begrunnelse.hvorfor"))
                .withKilde(BRUKER);

        XMLString hvaSokesOm = new XMLString()
                .withValue(webSoknad.getValueForFaktum("begrunnelse.hva"))
                .withKilde(BRUKER);

        return new XMLBegrunnelse()
                .withHvorforSoke(hvorforSoke)
                .withHvaSokesOm(hvaSokesOm);
    }
}